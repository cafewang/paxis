package org.wangyang.paxis.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import org.wangyang.paxis.entity.MinAcceptableProposal;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface MinAcceptableProposalRepository extends JpaRepository<MinAcceptableProposal, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<MinAcceptableProposal> findByInstanceNumber(Long instanceNumber);
}
