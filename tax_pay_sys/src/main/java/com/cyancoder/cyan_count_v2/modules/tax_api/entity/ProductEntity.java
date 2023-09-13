package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.entity;

import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.State;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "c_product")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "product_type")
    private ProductTypeEntity productType;

    private String name;

    private String code;

    private String unit;

    private BigDecimal tax;

    @Column(name = "sell_code")
    private String sellCode;

    @Column(name = "buy_code")
    private String buyCode;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    private Date createdOn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "edited_on")
    private Date editedOn;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "edited_by")
    private UserEntity editedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private State state;

    @Enumerated(EnumType.STRING)
    private Status status;



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ProductTypeEntity getProductType() {
        return productType;
    }

    public void setProductType(ProductTypeEntity productType) {
        this.productType = productType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public String getSellCode() {
        return sellCode;
    }

    public void setSellCode(String sellCode) {
        this.sellCode = sellCode;
    }

    public String getBuyCode() {
        return buyCode;
    }

    public void setBuyCode(String buyCode) {
        this.buyCode = buyCode;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getEditedOn() {
        return editedOn;
    }

    public void setEditedOn(Date editedOn) {
        this.editedOn = editedOn;
    }

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    public UserEntity getEditedBy() {
        return editedBy;
    }

    public void setEditedBy(UserEntity editedBy) {
        this.editedBy = editedBy;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }




}
