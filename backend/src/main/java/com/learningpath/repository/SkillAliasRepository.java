package com.learningpath.repository;

import com.learningpath.entity.SkillAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillAliasRepository extends JpaRepository<SkillAlias, Long> {
    Optional<SkillAlias> findByAliasIgnoreCase(String alias);
    List<SkillAlias> findByCanonicalSkillId(Long skillId);
}
