package com.learningpath.repository;

import com.learningpath.entity.ResourceSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResourceSkillRepository extends JpaRepository<ResourceSkill, Long> {
    List<ResourceSkill> findByResourceId(Long resourceId);
    List<ResourceSkill> findBySkillId(Long skillId);
}
