package com.learningpath.repository;

import com.learningpath.entity.RecommendationFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, Long> {
    List<RecommendationFeedback> findByRecommendationId(Long recommendationId);
}
