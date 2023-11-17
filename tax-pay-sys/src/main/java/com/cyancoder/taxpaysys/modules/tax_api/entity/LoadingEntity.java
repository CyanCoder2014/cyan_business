package com.cyancoder.taxpaysys.modules.tax_api.entity;

import com.cyancoder.taxpaysys.modules.tax_api.entity.factor.Factor;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.State;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.Status;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "c_loading")
public class LoadingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loading_id")
    private long id;

    @Column(name = "loading_number")
    private String loadingNumber;

    @Column(name = "persian_loading_date")
    private String persianLoadingDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "loading_date")
    private Date loadingDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "access_output_date")
    private Date accessOutputDate;

    private double weight;

    private BigDecimal price;

    @Column(name = "carrier_code")
    private String carrierCode;

    @Column(name = "buyer_carriage_fares")
    private BigDecimal buyerCarriageFares;

    @Column(name = "receiver_carriage_fares")
    private BigDecimal receiverCarriageFares;

    private BigDecimal discount;

    @ManyToOne
    @JoinColumn(name = "ord")
    private OrderEntity order;

    @ManyToOne
    @JoinColumn(name = "sub_factor")
    private Factor subFactor;

    @Column(name = "driver_full_name")
    private String driverFullName;

    @Column(name = "driver_first_phone_number")
    private String driverFirstPhoneNumber;

    @Column(name = "driver_second_phone_number")
    private String driverSecondPhoneNumber;

    @Column(name = "plate_number")
    private String plateNumber;

//    @Column(name = "is_edited")
//    private boolean isEdited;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "exported_on")
    private Date exportedOn;

    @Column(name = "marketplace_state")
    private String marketplaceState;

    @Column(name = "marketplace_message")
    private String marketplaceMessage;

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
