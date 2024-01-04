package com.cyancoder.factor.entity;

import com.cyancoder.factor.model.Status;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;


@Table(name = "f_product_types")
@Data
@Entity
public class ProductTypeEntity {


    @Id
    @Column(name = "product_type_id")
    private String productTypeId;


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
