package com.cyancoder.dynamiccore.store.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoredEntityDefinitionRepository extends JpaRepository<StoredEntityDefinition, Long> {
    Optional<StoredEntityDefinition> findByServiceKeyAndEntityKey(String serviceKey, String entityKey);
    List<StoredEntityDefinition> findByServiceKeyOrderByEntityKeyAsc(String serviceKey);
}
