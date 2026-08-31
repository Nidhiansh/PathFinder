package com.learningpath.repository;

import com.learningpath.entity.AssessmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssessmentSubmissionRepository extends JpaRepository<AssessmentSubmission, Long> {
    List<AssessmentSubmission> findByUserIdOrderBySubmittedAtDesc(Long userId);
    List<AssessmentSubmission> findByUserIdAndAssessmentId(Long userId, Long assessmentId);
}
