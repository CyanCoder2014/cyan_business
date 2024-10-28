package com.cyancoder.factor.entity;


import com.cyancoder.factor.model.FactorItemModel;
import com.cyancoder.factor.model.Status;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.BeanUtils;

import java.util.Date;


//@Builder
@NoArgsConstructor
@Table(name = "f_factor_items")
@Data
@Entity
@Slf4j
public class FactorItemEntity {

    public FactorItemEntity(FactorItemModel itemModel){
        BeanUtils.copyProperties(itemModel, this);
        this.setFactor(new FactorEntity(itemModel.getFactor()));
        this.setProduct(new ProductEntity(itemModel.getProduct()));

        log.info("this itemModel {}", itemModel.getProduct());
        log.info("this {}", this.getProduct());
        log.info("this {}", this);
    }


    @Id
    @Column(name = "factor_item_id")
    private String factorItemId;


    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @ManyToOne
    @JoinColumn(name = "factor_id")
    @JsonBackReference
    @JsonIgnore
    private FactorEntity factor;
//
//    @Column(name = "product_id")
//    private String productId;


    private Double amount;
    private Double price;

    private Double discount;
    private Double tax;
    private Double other_charge;

    private String detail;


    private String state;




    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;


    @Enumerated(EnumType.STRING)
    private Status status;}
