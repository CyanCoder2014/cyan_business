package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.entity;

import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.*;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * The persistent class for the c_user database table.
 */

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "c_order")
@JsonIdentityInfo(generator= ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
public class OrderEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private long id;

    @Column(name = "api_order_id")
    private String apiOrderId;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "unique_code")
    private String uniqueCode;

    @Column(name = "order_number")
    private String orderNumber;

    private String reference;

    private String period;

    //@OneToMany(mappedBy = "order",cascade = CascadeType.MERGE)
    @OneToMany(mappedBy = "order")
    private List<LoadingEntity> loadings;

//    @OneToMany(mappedBy = "order")
//    private List<SubFactorEntity> subFactor;


    @ManyToOne
    @JoinColumn(name = "product")
    private ProductEntity product;

    @JsonIgnoreProperties("orders")
    @ManyToOne
    @JoinColumn(name = "source")
    private SourceEntity source;

    @ManyToOne
    @JoinColumn(name = "buyer")
    private BuyerEntity buyer;

//    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
//    private ReceiverEntity receiver;

    private BigDecimal price;

//    @Column(name = "final_fee")
//    private BigDecimal finalFee;
//
//    @Column(name = "cash_fee")
//    private BigDecimal cashFee;

    private double weight;

    @Column(name = "load_balance")
    private BigDecimal loadBalance;

    @Column(name = "final_price")
    private BigDecimal finalPrice;

    @Column(name = "default_factor_price")
    private BigDecimal defaultFactorPrice;

    @Column(name = "pay_state")
    @Enumerated(EnumType.STRING)
    private PayState payState;

    @Column(name = "load_code")
    private String loadCode;

    @Column(name = "cost_reference")
    private String costReference;

    @Column(name = "delivery_type")
    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;

//    @Column(name = "factor_type")
//    @Enumerated(EnumType.STRING)
//    private FactorType factorType;

//    @Column(name = "factor_state")
//    @Enumerated(EnumType.STRING)
//    private FactorState factorState;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "clearance_id")
    private String clearanceId;

    private String details;

    @Column(name = "account_heading")
    private String accountHeading;

    @Column(name = "full_name")
    private String fullName;

    @ManyToOne
    @JoinColumn(name = "city")
    private CityEntity city;

    @Column(name = "national_code")
    private String nationalCode;

    @Column(name = "post_code")
    private String postCode;

    @Column(name = "tell_number")
    private String tellNumber;

    @Column(name = "fax_number")
    private String faxNumber;

    @Column(name = "first_address")
    private String firstAddress;

    @Column(name = "second_address")
    private String secondAddress;

//    @Column(name = "is_edited")
//    private boolean isEdited;

    @JsonIgnoreProperties("roles")
    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @JsonIgnoreProperties("roles")
    @ManyToOne
    @JoinColumn(name = "edited_by")
    private UserEntity editedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    private Date createdOn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "edited_on")
    private Date editedOn;

    @Enumerated(EnumType.STRING)
    private State state;

    @Enumerated(EnumType.STRING)
    private Status status;
}
