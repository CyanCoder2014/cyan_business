package com.cyancoder.processor.repository;

import com.cyancoder.processor.entity.ProcessorDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessorDefinitionRepository extends JpaRepository<ProcessorDefinition, Long> {
    Optional<ProcessorDefinition> findByProcessorKey(String processorKey);
}
