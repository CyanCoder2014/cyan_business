package com.cyancoder.factor.entity;


import jakarta.persistence.*;
import lombok.Data;



//@Builder
//@NoArgsConstructor
@Table(name = "f_factor_items")
@Data
@Entity
public class FactorItemEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String factorId;


    private String code;
}
