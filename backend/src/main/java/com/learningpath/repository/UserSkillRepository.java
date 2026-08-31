package com.learningpath.repository;

import com.learningpath.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByProfileId(Long profileId);
    List<UserSkill> findByProfileIdAndIsActiveTrue(Long profileId);
    Optional<UserSkill> findByProfileIdAndSkillId(Long profileId, Long skillId);
}
