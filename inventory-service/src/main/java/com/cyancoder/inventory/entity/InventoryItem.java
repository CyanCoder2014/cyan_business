package com.cyancoder.inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "inventory_items")
@Data
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String itemKey;

    private String catalogItemKey;
    private String warehouseKey;
    private BigDecimal onHandQuantity;
    private BigDecimal reservedQuantity;
    private BigDecimal reorderPoint;
    private String unit;
}
