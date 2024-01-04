package com.cyancoder.client.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;


@Table(name = "u_company")
@Data
@Entity
public class CompanyEntity {


    @Id
    @Column(name = "company_id")
    private String companyId;


    @Column(name = "client_id")
    private String clientId;


    private String name;

    @Column(name = "national_code")
    private Long nationalCode;

    @Column(name = "economic_code")
    private String economicCode;

    @Column(name = "unique_code")
    private String uniqueCode;

    private String pk;

    @Column(name = "legal_type")
    private String legalType;

    private String tell;

    private String address;

    @Column(name = "post_code")
    private String postCode;

    @Column(name = "city_id")
    private String cityId;
//    @ManyToOne
//    @JoinColumn(name = "city_i")
//    private CityEntity city;


    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdOn;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "edited_at")
    private Date editedOn;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @LastModifiedBy
    @Column(name = "edited_by")
    private String editedBy;

//    @Enumerated(EnumType.STRING)
//    private State state;
//
//    @Enumerated(EnumType.STRING)
//    private Status status;


}
