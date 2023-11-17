package com.cyancoder.taxpaysys.modules.tax_api.entity;

import com.cyancoder.taxpaysys.modules.tax_api.entity.general.State;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.Status;
import jakarta.persistence.*;

import java.util.List;

/**
 * The persistent class for the c_user database table.
 */

@Entity
@Table(name = "c_city")
public class CityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "city_id")
    private long id;

    private String code;

    private String name;

    @ManyToOne
    @JoinColumn(name = "ste")
    private StateEntity ste;

    @Column(name = "dial_code")
    private String dialCode;

    @Column(name = "first_api_order_code")
    private String firstApiOrderCode;

    @Column(name = "second_api_order_code")
    private String secondApiOrderCode;

    @OneToMany(mappedBy = "city")
    private List<BuyerEntity> buyers;

    @Enumerated(EnumType.STRING)
    private State state;

    @Enumerated(EnumType.STRING)
    private Status status;
}
