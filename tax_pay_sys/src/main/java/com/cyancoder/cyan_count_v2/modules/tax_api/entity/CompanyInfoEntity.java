package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.entity;


import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.factor.Factor;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.State;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.Status;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * The persistent class for the c_user database table.
 */
@Entity
@Table(name = "c_company_info")
public class CompanyInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_info_id")
    private long id;

    private String name;

    @Column(name = "national_code")
    private String nationalCode;

    @Column(name = "economic_code")
    private String economicCode;

    @Column(name = "register_code")
    private String registerCode;

    @Column(name = "register_place")
    private String registerPlace;

    private String phone;

    private String 	fax;


    @Column(name = "post_code")
    private String postCode;

    private String 	address;

    @Enumerated(EnumType.STRING)
    private State state;

    @Enumerated(EnumType.STRING)
    private Status status;






    public CompanyInfoEntity() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNationalCode() {
        return nationalCode;
    }

    public void setNationalCode(String nationalCode) {
        this.nationalCode = nationalCode;
    }

    public String getEconomicCode() {
        return economicCode;
    }

    public void setEconomicCode(String economicCode) {
        this.economicCode = economicCode;
    }

    public String getRegisterCode() {
        return registerCode;
    }

    public void setRegisterCode(String registerCode) {
        this.registerCode = registerCode;
    }

    public String getRegisterPlace() {
        return registerPlace;
    }

    public void setRegisterPlace(String registerPlace) {
        this.registerPlace = registerPlace;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }




    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

//    public List<SourceEntity> getSources() {
//        return sources;
//    }

//    public void setSources(List<SourceEntity> sources) {
//        this.sources = sources;
//    }



//    public List<BuyDetailEntity> getBuyDetails() {
//        return buyDetails;
//    }

//    public void setBuyDetails(List<BuyDetailEntity> buyDetails) {
//        this.buyDetails = buyDetails;
//    }





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