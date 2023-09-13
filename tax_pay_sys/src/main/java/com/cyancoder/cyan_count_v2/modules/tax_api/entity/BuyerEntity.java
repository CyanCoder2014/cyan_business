package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.entity;


import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.Person;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.State;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.Status;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * The persistent class for the c_user database table.
 */

@Entity
//@SqlResultSetMapping(name = "MonthlySalesChartEntity",
//        classes = @ConstructorResult(
//                targetClass = MonthlySalesChartEntity.class,
//                columns = {
//                        @ColumnResult(name = "product_name", type = String.class),
//                        @ColumnResult(name = "total_order", type = Long.class),
//                        @ColumnResult(name = "order_weight", type = BigDecimal.class),
//                        @ColumnResult(name = "total_loading", type = Long.class),
//                        @ColumnResult(name = "loading_weight", type = BigDecimal.class),
//                        @ColumnResult(name = "persian_date", type = String.class)
//                }
//        )
//)
@Data
@Table(name = "c_buyer")
public class BuyerEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "buyer_id")
    private long id;

    private String code;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "national_code")
    private String nationalCode;

    @Enumerated(EnumType.STRING)
    private Person person;

    private String details;

    private BigDecimal credit;

    @Column(name = "economic_code")
    private String economicCode;

    @Column(name = "accounting_code")
    private String accountingCode;

    @Column(name = "post_code")
    private String postCode;

    @Column(name = "tell_number")
    private String tellNumber;

    @Column(name = "fax_number")
    private String faxNumber;

    private String address;

    @OneToMany(mappedBy = "buyer")
    private List<OrderEntity> orders;


    @ManyToOne
    @JoinColumn(name = "buyer_type")
    private BuyerTypeEntity buyerType;

    @ManyToOne
    @JoinColumn(name = "city")
    private CityEntity city;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    private Date createdOn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "edited_on")
    private Date editedOn;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @ManyToOne
    @JoinColumn(name = "edited_by")
    private UserEntity editedBy;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private State state;
}
