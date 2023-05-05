package org.wangyang.paxis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(AcceptedProposalValueId.class)
public class AcceptedProposalValue {
    @Id
    private Long instanceNumber;
    @Id
    private Long proposalNumber;
    private String proposalValue;
}
