package com.cyancoder.taxpaysys.modules.tax_api.entity.factor;


import com.cyancoder.taxpaysys.modules.tax_api.entity.*;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "c_sub_factor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Factor  implements Serializable{

    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_factor_id")
    private Long id;


        private String code;

        @ManyToOne
        @JoinColumn(name = "seller")
        private CompanyInfoEntity seller;

        @ManyToOne
        @JoinColumn(name = "product_type")
        private ProductTypeEntity productType;

        @ManyToOne
        @JoinColumn(name = "product_id")
        private ProductEntity product;


        @Column(name = "product_name")
        private String productName;

//        @ManyToOne
//        @JoinColumn(name = "ord")
//        private OrderEntity order;

//        @OneToMany(mappedBy = "subFactor")
//        private List<LoadingEntity> loadings;

        private long numbers;

        private String unit;

        @Column(name = "unit_price")
        private BigDecimal unitPrice;

        private BigDecimal discount;

        @Column(name = "final_price")
        private BigDecimal finalPrice;

        @Column(name = "factor_date")
        private Date factorDate;

        private BigDecimal tax;

        private BigDecimal weight;

        @Column(name = "price_plus_tax")
        private BigDecimal pricePlusTax;

        @Column(name = "sell_type")
        @Enumerated(EnumType.STRING)
        private SellType sellType;


        @Column(name = "pay_state")
        @Enumerated(EnumType.STRING)
        private PayState payState;

        private String distribution;

        @ManyToOne
        @JoinColumn(name = "buyer_type")
        private BuyerTypeEntity buyerType;

        @Column(name = "full_name")
        private String fullName;

        @Column(name = "national_code")
        private String nationalCode;

        @Enumerated(EnumType.STRING)
        private Person person;

        @Column(name = "register_number")
        private String registerNumber;

        @ManyToOne
        @JoinColumn(name = "city")
        private CityEntity city;

        private String address;

        @Column(name = "post_code")
        private String postCode;

        @Column(name = "tell_number")
        private String tellNumber;

        @Column(name = "economic_code")
        private String economicCode;

        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "created_on")
        private Date createdOn;

        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "edited_on")
        private Date editedOn;

//        @ManyToOne
//        @JoinColumn(name = "created_by")
//        private UserEntity createdBy;
//
//        @ManyToOne
//        @JoinColumn(name = "edited_by")
//        private UserEntity editedBy;

        @Enumerated(EnumType.STRING)
        private State state;

        @Enumerated(EnumType.STRING)
        private Status status;


    @Column(name = "tax_api_uid")
    private String taxApiUid;

    @Column(name = "tax_api_refrence")
    private String taxApiRefrence;

    @Column(name = "tax_api_state")
    private Integer taxApiState;

    @Column(name = "tax_api_message")
    private String taxApiMessage;

    }
