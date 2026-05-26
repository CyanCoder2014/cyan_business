package com.cyancoder.taxpaysys.modules.tax_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;


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


    @Column(name = "successed_at")
    private Date successesAt;

    @Column(name = "tax_api_successed_uid")
    private String taxApiSuccessesUid;

    @Column(name = "tax_api_successed_reference")
    private String taxApiSuccessesReference;



    @Column(name = "tax_api_correction_uid")
    private String taxApiCorrectionUid;

    @Column(name = "tax_api_correction_reference")
    private String taxApiCorrectionReference;

    @Column(name = "tax_api_cancellation_uid")
    private String taxApiCancellationUid;

    @Column(name = "tax_api_cancellation_reference")
    private String taxApiCancellationReference;


    @Column(name = "tax_api_state")
    private String taxApiState; // need enum

    @Column(name = "tax_api_message")
    private String taxApiMessage;

    @Column(name = "tax_api_data")
    private String taxApiData;

    @Column(name = "client_id")
    private String clientId;


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




//    @Enumerated(EnumType.STRING)
//    private Status status;
}
