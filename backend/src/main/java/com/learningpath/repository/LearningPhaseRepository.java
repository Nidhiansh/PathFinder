package com.learningpath.repository;

import com.learningpath.entity.LearningPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LearningPhaseRepository extends JpaRepository<LearningPhase, Long> {
    List<LearningPhase> findByLearningPathIdOrderByPhaseNumberAsc(Long learningPathId);
}
