package com.cyancoder.factor.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;


@Table(name = "f_factor_item")
@Data
@Builder
@Entity
public class FactorItemEntity {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_factor_id")
    private Long id;


    private String code;
}
