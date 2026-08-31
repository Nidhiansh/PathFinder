package com.learningpath.repository;

import com.learningpath.entity.LearningResource;
import com.learningpath.entity.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LearningResourceRepository extends JpaRepository<LearningResource, Long> {
    List<LearningResource> findByResourceType(ResourceType resourceType);
}
