package com.cyancoder.taxpaysys.modules.tax_api.entity;


import com.cyancoder.taxpaysys.modules.tax_api.entity.general.State;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.Status;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "c_transportation")
public class TransportationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private String code;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private State state;

    @Enumerated(EnumType.STRING)
    private Status status;

}
