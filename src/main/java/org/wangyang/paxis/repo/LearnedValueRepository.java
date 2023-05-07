package org.wangyang.paxis.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.wangyang.paxis.entity.LearnedValue;

@Repository
public interface LearnedValueRepository extends JpaRepository<LearnedValue, Long> {
}
