package org.wangyang.paxis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextProposalNumber {
    @Id
    private Long instanceNumber;

    private Long proposalNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        NextProposalNumber that = (NextProposalNumber) o;
        return getInstanceNumber() != null && Objects.equals(getInstanceNumber(), that.getInstanceNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(instanceNumber);
    }
}
