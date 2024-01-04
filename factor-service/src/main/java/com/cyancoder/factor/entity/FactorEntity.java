package com.cyancoder.factor.entity;

import com.cyancoder.factor.model.FactorModel;
import com.cyancoder.factor.model.Status;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;


@Table(name = "f_factors")
@Data
@Entity
public class FactorEntity {

    public FactorEntity(FactorModel factorModel){
        BeanUtils.copyProperties(factorModel, this);

    }

    public FactorEntity(){

    }

//    private static final long serialVersionUID = 5313493413859894403L;

//    @GeneratedValue(strategy = GenerationType.IDENTITY)


    @jakarta.persistence.Id
    @Column(name = "factor_id")
    private String factorId;

    private String code;

    @OneToMany(mappedBy = "factor")
    private List<FactorItemEntity> items;

    @Column(name = "company_id")
    private String companyId;

    @Column(name = "buyer_id")
    private String buyerId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "factor_date")
    private Date factorDate;

    @Column(name = "pay_type")
    private String payType;

    private Double payed;


    private String state;
    private String note;

//    @ManyToOne
//    @JoinColumn(name = "product_id")
//    private ProductEntity product;


//    @Column(name = "pay_state")
//    @Enumerated(EnumType.STRING)
//    private PayState payState;


//    @ManyToOne
//    @JoinColumn(name = "buyer_id")
//    private BuyerEntity buyer;


//    @ManyToOne
//    @JoinColumn(name = "city_i")
//    private CityEntity city;


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
//
//    @Enumerated(EnumType.STRING)
//    private Status status;


//    @Column(name = "tax_api_uid")
//    private String taxApiUid;
//
//    @Column(name = "tax_api_reference")
//    private String taxApiReference;
//
//    @Column(name = "tax_api_state")
//    private String taxApiState; // need enum
//
//    @Column(name = "tax_api_message")
//    private String taxApiMessage;


    @Enumerated(EnumType.STRING)
    private Status status;
}
