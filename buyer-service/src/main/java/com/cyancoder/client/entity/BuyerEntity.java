package com.cyancoder.client.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;


@Table(name = "b_buyer")
@Data
@Entity
public class BuyerEntity {


    @Id
    @Column(name = "buyer_id")
    private String buyerId;


    private String note;

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


}
