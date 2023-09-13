package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.entity;


import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.State;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.Status;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "c_source")
@JsonIdentityInfo(generator= ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
public class SourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "source_id")
    private long id;

    private String code;

//        @ManyToOne
//        @JoinColumn(name = "load_license")
//        private LoadLicenseEntity loadLicense;

    @ManyToOne
    @JoinColumn(name = "product")
    private ProductEntity product;

    @Column(name = "perform_number")
    private String performNumber;

    @Column(name = "tax_percentage")
    private BigDecimal taxPercentage;

    private String unit;

    @Column(name = "initial_amount")
    private double initialAmount;

    @Column(name = "origin_name")
    private String originName;

    @Column(name = "final_amount")
    private double finalAmount;

//        @ManyToOne
//        @JoinColumn(name = "ship")
//        private ShipEntity ship;

        @ManyToOne
        @JoinColumn(name = "transportation")
        private TransportationEntity transportation;

        @ManyToOne
        @JoinColumn(name = "seller")
        private CompanyInfoEntity seller;

    @Column(name = "accounting_code")
    private String accountingCode;

    @Column(name = "seller_name")
    private String sellerName;

    @Column(name = "customs_clearance")
    private BigDecimal customsClearance;

    @Column(name = "carrier_institute")
    private String carrierInstitute;

    @OneToMany(mappedBy = "source")
    private List<OrderEntity> orders;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    private Date createdOn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "edited_on")
    private Date editedOn;

    @JsonIgnoreProperties("roles")
    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @JsonIgnoreProperties("roles")
    @ManyToOne
    @JoinColumn(name = "edited_by")
    private UserEntity editedBy;

    @Enumerated(EnumType.STRING)
    private State state;

    @Enumerated(EnumType.STRING)
    private Status status;

}
