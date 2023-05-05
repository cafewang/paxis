package org.wangyang.paxis.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.wangyang.paxis.entity.AcceptedProposalValue;
import org.wangyang.paxis.entity.AcceptedProposalValueId;

import java.util.Optional;

@Repository
public interface AcceptedProposalValueRepository extends JpaRepository<AcceptedProposalValue, AcceptedProposalValueId> {
    Optional<AcceptedProposalValue> findTopOneByInstanceNumberAndProposalNumberLessThanOrderByProposalNumberDesc(Long instanceNumber, Long proposalNumber);
}
