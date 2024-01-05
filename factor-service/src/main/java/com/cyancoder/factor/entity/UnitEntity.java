package com.cyancoder.factor.entity;

import com.cyancoder.factor.model.ProductModel;
import com.cyancoder.factor.model.Status;
import com.cyancoder.factor.model.UnitModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.BeanUtils;

import java.util.Date;


@Table(name = "f_units")
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UnitEntity {


    public UnitEntity(UnitModel unitModel){
        BeanUtils.copyProperties(unitModel, this);

    }

    @Id
    @Column(name = "unit_id")
    private String unitId;

    private String code;
    private String name;

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
