package com.cyancoder.client.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;


@Table(name = "b_buyer")
@Data
@Entity
public class BuyerEntity {


    @Id
    @Column(name = "buyer_id")
    private String buyerId;

    @Column(name = "national_code")
    private Long nationalCode;

    @Column(name = "economic_code")
    private String economicCode;

    @Column(name = "buyer_type")
    private String buyerType;
    private String tell;
    private String address;

    @Column(name = "post_code")
    private String postCode;

    @Column(name = "city_id")
    private String cityId;
    private String note;

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


}
