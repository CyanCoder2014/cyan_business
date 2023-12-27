package com.cyancoder.factor.entity;


import com.cyancoder.factor.model.FactorItemModel;
import com.cyancoder.factor.model.Status;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Date;


//@Builder
@NoArgsConstructor
@Table(name = "f_factor_items")
@Data
@Entity
public class FactorItemEntity {

    public FactorItemEntity(FactorItemModel itemModel){
        BeanUtils.copyProperties(itemModel, this);
    }


    @Id
    @Column(name = "factor_item_id")
    private String factorItemId;

    @Column(name = "factor_id")
    private String factorId;

    @Column(name = "product_id")
    private String productId;
    @Column(name = "unit_id")
    private String unitId;


    private Double amount;
    private Double price;

    private Double discount;
    private Double tax;
    private Double other_charge;

    private String detail;


    private String state;




    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    private Date createdOn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "edited_on")
    private Date editedOn;


    @Enumerated(EnumType.STRING)
    private Status status;}
