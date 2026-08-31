package com.learningpath.repository;

import com.learningpath.entity.SkillRelation;
import com.learningpath.entity.SkillRelationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRelationRepository extends JpaRepository<SkillRelation, Long> {
    List<SkillRelation> findBySourceSkillId(Long sourceSkillId);
    List<SkillRelation> findByTargetSkillId(Long targetSkillId);
    List<SkillRelation> findByTargetSkillIdAndRelationType(Long targetSkillId, SkillRelationType relationType);
    List<SkillRelation> findBySourceSkillIdAndRelationType(Long sourceSkillId, SkillRelationType relationType);
}
