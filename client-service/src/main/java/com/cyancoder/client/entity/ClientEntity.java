package com.cyancoder.client.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;


@Table(name = "u_client")
@Data
@Entity
public class ClientEntity {


    @Id
    @Column(name = "client_id")
    private String clientId;


    private String name;


//    private String tell;///////
    /////


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdOn;

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


}
