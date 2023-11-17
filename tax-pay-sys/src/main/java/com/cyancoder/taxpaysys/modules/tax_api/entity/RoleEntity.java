package com.cyancoder.taxpaysys.modules.tax_api.entity;


import com.cyancoder.taxpaysys.modules.tax_api.entity.general.State;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.Status;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "c_role")
public class RoleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private long               id;


    private String            des;

    private String            name;

    @ManyToMany
    @JoinTable(
            name = "permission_role"
            , joinColumns = {
            @JoinColumn(name = "role_id")
    }
            , inverseJoinColumns = {
            @JoinColumn(name = "permission_id")
    })
    private Set<PermissionEntity> permissions;

    @Enumerated(EnumType.STRING)
    private State state;

    @Enumerated(EnumType.STRING)
    private Status status;}
