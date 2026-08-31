package com.learningpath.repository;

import com.learningpath.entity.SkillPrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SkillPrerequisiteRepository extends JpaRepository<SkillPrerequisite, Long> {
    List<SkillPrerequisite> findBySkillId(Long skillId);
    List<SkillPrerequisite> findByPrerequisiteSkillId(Long prerequisiteSkillId);
}
