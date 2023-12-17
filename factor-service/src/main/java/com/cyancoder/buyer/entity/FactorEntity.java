package com.cyancoder.buyer.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;


@Table(name = "f__factor")
@Data
@Entity
public class FactorEntity {


//    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_factor_id")
    private Long id;


    private String code;

//    @ManyToOne
//    @JoinColumn(name = "seller_id")
//    private SellerEntity seller;


//    @ManyToOne
//    @JoinColumn(name = "product_id")
//    private ProductEntity product;



//    @Column(name = "pay_state")
//    @Enumerated(EnumType.STRING)
//    private PayState payState;

    private String note;

//    @ManyToOne
//    @JoinColumn(name = "buyer_id")
//    private BuyerEntity buyer;


//    @ManyToOne
//    @JoinColumn(name = "city_i")
//    private CityEntity city;




    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    private Date createdOn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "edited_on")
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


    @Column(name = "tax_api_uid")
    private String taxApiUid;

    @Column(name = "tax_api_reference")
    private String taxApiReference;

    @Column(name = "tax_api_state")
    private String taxApiState; // need enum

    @Column(name = "tax_api_message")
    private String taxApiMessage;
}
