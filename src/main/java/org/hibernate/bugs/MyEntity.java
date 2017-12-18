package org.hibernate.bugs;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MyEntity {
    @GeneratedValue
    @Id
    public int id;
}
