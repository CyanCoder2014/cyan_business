package com.cyancoder.factor.entity;

import com.cyancoder.factor.model.FactorModel;
import com.cyancoder.factor.model.ProductModel;
import com.cyancoder.factor.model.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;


@Table(name = "f_products")
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

    public ProductEntity(ProductModel productModel){
        BeanUtils.copyProperties(productModel, this);
        this.setUnit(new UnitEntity(productModel.getUnit()));
    }


    @Id
    @Column(name = "product_id")
    private String productId;


    @ManyToOne
    @JoinColumn(name = "product_type_id")
    private ProductTypeEntity productType;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private UnitEntity unit;

    private String code;
    private String name;
    private String note;

    private String state;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

//        @ManyToOne
//        @JoinColumn(name = "created_by")
//        private UserEntity createdBy;
//
//        @ManyToOne
//        @JoinColumn(name = "edited_by")
//        private UserEntity editedBy;

//    @Enumerated(EnumType.STRING)
//    private State state;

    @Enumerated(EnumType.STRING)
    private Status status;
}
