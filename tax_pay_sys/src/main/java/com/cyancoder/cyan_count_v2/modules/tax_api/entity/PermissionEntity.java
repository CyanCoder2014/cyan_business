package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.entity;


import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.State;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.Status;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "c_permission")
public class PermissionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private long               id;


    private String            category;

    private String            des;

    private String            label;

    private String            name;

    @Enumerated(EnumType.STRING)
    private State state;

    @Enumerated(EnumType.STRING)
    private Status status;


}
