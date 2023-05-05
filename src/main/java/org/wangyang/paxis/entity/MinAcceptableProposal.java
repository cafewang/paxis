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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MinAcceptableProposal {
    @Id
    private Long instanceNumber;

    private Long proposalNumber;

    public static MinAcceptableProposal create(Long instanceNumber, Long proposalNumber) {
        return MinAcceptableProposal.builder()
                .instanceNumber(instanceNumber)
                .proposalNumber(proposalNumber)
                . build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        MinAcceptableProposal that = (MinAcceptableProposal) o;
        return getInstanceNumber() != null && Objects.equals(getInstanceNumber(), that.getInstanceNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(instanceNumber);
    }
}
