package com.learningpath.repository;

import com.learningpath.entity.LearnerProfile;
import com.learningpath.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LearnerProfileRepository extends JpaRepository<LearnerProfile, Long> {
    Optional<LearnerProfile> findByUser(User user);
    Optional<LearnerProfile> findByUserId(Long userId);
}
