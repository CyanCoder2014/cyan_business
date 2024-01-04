package com.cyancoder.taxpaysys.modules.tax_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;


@Table(name = "t_factor_tax")
@Data
@Entity
public class FactorTaxEntity {

//    public FactorTaxEntity(FactorModel factorModel){
//        BeanUtils.copyProperties(factorModel, this);
//
//    }

    public FactorTaxEntity(){

    }

    @Id
    @Column(name = "factor_tax_id")
    private String factorTaxId;

    @Column(name = "factor_id")
    private String factorId;



    @Column(name = "tax_api_uid")
    private String taxApiUid;

    @Column(name = "tax_api_reference")
    private String taxApiReference;

    @Column(name = "tax_api_state")
    private String taxApiState; // need enum

    @Column(name = "tax_api_message")
    private String taxApiMessage;

    @Column(name = "client_id")
    private String clientId;


    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdOn;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "edited_at")
    private Date editedOn;

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




//    @Enumerated(EnumType.STRING)
//    private Status status;
}
