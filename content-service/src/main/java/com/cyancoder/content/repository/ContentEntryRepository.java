package com.cyancoder.content.repository;

import com.cyancoder.content.entity.ContentEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContentEntryRepository extends JpaRepository<ContentEntry, Long> {
    Optional<ContentEntry> findByKey(String key);
    Optional<ContentEntry> findBySlug(String slug);
    List<ContentEntry> findByContentType(String contentType);
}
