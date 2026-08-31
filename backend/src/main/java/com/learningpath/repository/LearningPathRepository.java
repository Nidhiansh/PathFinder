package com.learningpath.repository;

import com.learningpath.entity.LearningPath;
import com.learningpath.entity.PathStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {
    Optional<LearningPath> findByUserIdAndStatus(Long userId, PathStatus status);
    List<LearningPath> findByUserIdOrderByCreatedAtDesc(Long userId);
}
