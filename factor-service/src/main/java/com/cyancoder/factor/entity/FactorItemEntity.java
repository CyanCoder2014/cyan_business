package com.cyancoder.factor.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.Id;



//@Builder
//@NoArgsConstructor
@Table(name = "f_factor_items")
@Data
@Entity
public class FactorItemEntity {


    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    private String code;
}
