package org.wangyang.paxis.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class AcceptedProposalValueId implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long instanceNumber;
    private Long proposalNumber;
}
