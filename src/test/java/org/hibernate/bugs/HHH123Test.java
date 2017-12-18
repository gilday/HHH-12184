package org.hibernate.bugs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * NullPointerException in {{LiteralExpression#renderProjection}}. {{ValueHandlerFactory.determineAppropriateHandler(
 * literal.getClass() );}} returns null for enum classes. Triggered when constructing a new result object using a
 * selectCase criteria.
 */
public class HHH123Test {

    private EntityManagerFactory entityManagerFactory;

    @Before
    public void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("templatePU");
    }

    @After
    public void destroy() {
        entityManagerFactory.close();
    }

    // Entities are auto-discovered, so just add them anywhere on class-path
    // Add your tests, using standard JUnit.
    @Test
    public void test() {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();

        // insert a few MyEntity instances
        em.persist(new MyEntity());
        em.persist(new MyEntity());
        em.flush();

        // query for Foo results using construct and selectCase
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Foo> query = cb.createQuery(Foo.class);
        final Root<MyEntity> root = query.from(MyEntity.class);

        query
                .select(cb.construct(Foo.class,
                        cb.selectCase()
                                .when(cb.equal(root.get("id"), 1), Foo.State.FIRST)
                                .otherwise(Foo.State.NOT_FIRST),
                        root.get(org.hibernate.bugs.MyEntity_.id)
                ))
                .orderBy(cb.asc(root.get(org.hibernate.bugs.MyEntity_.id)));

        final List<Foo> foos = em.createQuery(query).getResultList();
        assertThat(foos.size(), equalTo(2));
        assertThat(foos.get(0).state(), equalTo(Foo.State.FIRST));
        assertThat(foos.get(1).state(), equalTo(Foo.State.NOT_FIRST));

        em.getTransaction().commit();
        em.close();
    }

    @Accessors(fluent = true)
    @Getter
    @RequiredArgsConstructor
    public static class Foo {
        enum State {FIRST, NOT_FIRST}

        private final State state;
        private final int id;
    }
}
