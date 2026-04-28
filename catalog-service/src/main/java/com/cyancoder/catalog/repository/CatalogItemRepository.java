package com.cyancoder.catalog.repository;

import com.cyancoder.catalog.entity.CatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CatalogItemRepository extends JpaRepository<CatalogItem, Long> {
    Optional<CatalogItem> findByItemKey(String itemKey);
    List<CatalogItem> findByItemType(String itemType);
}
