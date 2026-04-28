package com.cyancoder.commerce.repository;

import com.cyancoder.commerce.entity.CommerceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommerceDocumentRepository extends JpaRepository<CommerceDocument, Long> {
    Optional<CommerceDocument> findByDocumentKey(String documentKey);
    List<CommerceDocument> findByDocumentType(String documentType);
}
