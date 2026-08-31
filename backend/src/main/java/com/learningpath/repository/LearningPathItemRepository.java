package com.learningpath.repository;

import com.learningpath.entity.LearningPathItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LearningPathItemRepository extends JpaRepository<LearningPathItem, Long> {
    List<LearningPathItem> findByLearningPhaseIdOrderByOrderIndexAsc(Long learningPhaseId);
}
