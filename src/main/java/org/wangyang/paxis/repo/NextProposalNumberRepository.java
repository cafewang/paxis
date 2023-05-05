package org.wangyang.paxis.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.wangyang.paxis.entity.NextProposalNumber;

@Repository
public interface NextProposalNumberRepository extends JpaRepository<NextProposalNumber, Long> {
}
