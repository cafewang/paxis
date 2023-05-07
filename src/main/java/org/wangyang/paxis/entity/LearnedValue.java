package org.wangyang.paxis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnedValue {
    @Id
    private Long instanceNumber;
    private String proposalValue;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        LearnedValue that = (LearnedValue) o;
        return getInstanceNumber() != null && Objects.equals(getInstanceNumber(), that.getInstanceNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(instanceNumber);
    }
}
