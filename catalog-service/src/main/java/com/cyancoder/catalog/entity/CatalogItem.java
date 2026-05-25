package com.cyancoder.catalog.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "catalog_items")
@Data
public class CatalogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String itemKey;

    @Column(nullable = false)
    private String itemType;

    private String name;
    private String sku;
    private String categoryKey;
    private String unit;
    private BigDecimal defaultPrice;
    private String currency;
    private Boolean active;

    @Column(length = 4000)
    private String attributesJson;
}
