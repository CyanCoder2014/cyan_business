package com.cyancoder.aiorchestrator.repo;

import com.cyancoder.aiorchestrator.domain.AppBlueprint;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AppBlueprintRepository extends MongoRepository<AppBlueprint, String> {
    List<AppBlueprint> findByActiveTrueOrderByAppTypeAscVersionDesc();
    Optional<AppBlueprint> findFirstByAppTypeAndActiveTrueOrderByVersionDesc(String appType);
    Optional<AppBlueprint> findFirstByBlueprintKeyAndActiveTrueOrderByVersionDesc(String blueprintKey);
}
