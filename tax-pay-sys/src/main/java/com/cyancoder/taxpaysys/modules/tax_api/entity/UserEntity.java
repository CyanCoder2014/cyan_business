package com.cyancoder.taxpaysys.modules.tax_api.entity;

import com.cyancoder.taxpaysys.modules.tax_api.entity.general.State;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.Status;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
@Entity
@Data
@Table(name = "c_user")
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private int ord;

    private String username;

    private String password;

    private String token;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "token_life_time")
    private Date tokenLifeTime;

    @ManyToMany
    @JoinTable(
            name = "role_user"
            , joinColumns = {
            @JoinColumn(name = "user_id")
    }
            , inverseJoinColumns = {
            @JoinColumn(name = "role_id")
    })
    private Set<RoleEntity> roles;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    private Date createdOn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "edited_on")
    private Date editedOn;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @ManyToOne
    @JoinColumn(name = "edited_by")
    private UserEntity editedBy;


    @Enumerated(EnumType.STRING)
    private State state;

    @Enumerated(EnumType.STRING)
    private Status status;}
