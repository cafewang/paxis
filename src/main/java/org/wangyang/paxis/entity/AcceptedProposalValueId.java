package org.wangyang.paxis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptedProposalValueId implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long instanceNumber;
    private Long proposalNumber;
}
