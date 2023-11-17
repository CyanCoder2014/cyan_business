package com.cyancoder.taxpaysys.modules.tax_api.entity;


import java.math.BigDecimal;
import java.util.Date;


public class FactorEntity {

    private Long id;
    private Boolean sarjam;// سر جمع
    private Boolean azTarigheHagholAmalkari;// انجام معامله از طریق حق العمل کاری
    private String productTypeCode;// نوع کالا
    private String productTypeName;// نوع کالا
    private String productDescription;// شرح کالا یا خدمات
    private String productCode;//شناسه کالا
    private Boolean bargashti;// برگشتی
    private BigDecimal productAmount;// مبلغ کالا یا خدمت
    private BigDecimal addedValueAmount;// مالیات بر ارزش افزوده
    private BigDecimal taxAmount;// عوارض
    private BigDecimal otherTaxAmount;// سایر عوارض
    private BigDecimal discountAmount;// مبلغ تخفیف
    private String person;// عدد نوع فروشنده با توجه به توضیحات
    private String postCode;// کد پستی
    private String areaCode;// پیش کد شهر
    private String tellNumber;// تلفن
    private String address;// آدرس فروشنده
    private String fullName;// نام فروشنده
    private String companyName;// نام شرکت/
    private String economicCode;// کد اقتصادی
    private String nationalCode; // کدملی/ شناسنامه
    private String sellerTypeCode;// نوع فروشنده
    private String sellerTypeName;// نوع فروشنده
    private String stateCode;// کد استان
    private String stateName;// نام استان
    private String cityCode;// کدشهر
    private String cityName;// نام شهر
    private String currencyCode;// نوع ارز
    private String currencyName;// نوع ارز
    private BigDecimal productCurrencyAmount;// مبلغ ارزی کالا یا خدمت
    private BigDecimal addedValueCurrencyAmount;// مبلغ ارزی ارزش افزوده
    private BigDecimal taxCurrencyAmount;// مبلغ ارزی عوارض
    private BigDecimal otherTaxCurrencyAmount;// مبلغ ارزی سایر عوارض
    private BigDecimal discountCurrencyAmount;// مبلغ ارزی تخفیف
    private BigDecimal productExchangeRate;// نرخ برابری ارز مبلغ کالا یا خدمت
    private BigDecimal discountExchangeRate;// نرخ برابری ارز تخفیف
    private BigDecimal addedValueExchangeRate;// ArzBarabari_MaliatArzeshAfzoodeh
    private BigDecimal taxExchangeRate;// ArzBarabari_AvarezArzeshAfzoodeh
    private BigDecimal otherTaxExchangeRate;// ArzBarabari_SayerAvarez
    private BigDecimal productRialAmount;// معادل ریالی مبلغ ارزی کالا یا خدمت
    private BigDecimal discountRialAmount;// معادل ریالی مبلغ ارزی تخفیف
    private BigDecimal addedValueRialAmount;// MoadelRialiMaliatArzeshAfzoodeh
    private BigDecimal taxRialAmount;// MoadelRialiAvarezArzeshAfzoodeh
    private BigDecimal otherTaxRialAmount;// MoadelRialiSayerAvarez
    private String billCode;// شماره صورتحساب
    private Date billDate;// تاریخ صورتحساب
    private String accountingDocumentCode;// شماره سند حسابداری
    private Date accountingDocumentDate;// تاریخ صورتحساب
    private Boolean isSent;// IsSent

    private Integer type;// 1 = buy-detail & 0 = sell-detail

    public FactorEntity() {
    }

    public FactorEntity(Long id, Boolean sarjam, Boolean azTarigheHagholAmalkari, String productTypeCode, String productTypeName, String productDescription, String productCode, Boolean bargashti, BigDecimal productAmount, BigDecimal addedValueAmount, BigDecimal taxAmount, BigDecimal otherTaxAmount, BigDecimal discountAmount, String person, String postCode, String areaCode, String tellNumber, String address, String fullName, String companyName, String economicCode, String nationalCode, String sellerTypeCode, String sellerTypeName, String stateCode, String stateName, String cityCode, String cityName, String currencyCode, String currencyName, BigDecimal productCurrencyAmount, BigDecimal addedValueCurrencyAmount, BigDecimal taxCurrencyAmount, BigDecimal otherTaxCurrencyAmount, BigDecimal discountCurrencyAmount, BigDecimal productExchangeRate, BigDecimal discountExchangeRate, BigDecimal productRialAmount, BigDecimal discountRialAmount, String billCode, Date billDate, String accountingDocumentCode, Date accountingDocumentDate, Boolean isSent, BigDecimal addedValueExchangeRate, BigDecimal taxExchangeRate, BigDecimal otherTaxExchangeRate, BigDecimal addedValueRialAmount, BigDecimal taxRialAmount, BigDecimal otherTaxRialAmount, Integer type) {
        this.id = id;
        this.sarjam = sarjam;
        this.azTarigheHagholAmalkari = azTarigheHagholAmalkari;
        this.productTypeCode = productTypeCode;
        this.productTypeName = productTypeName;
        this.productDescription = productDescription;
        this.productCode = productCode;
        this.bargashti = bargashti;
        this.productAmount = productAmount;
        this.addedValueAmount = addedValueAmount;
        this.taxAmount = taxAmount;
        this.otherTaxAmount = otherTaxAmount;
        this.discountAmount = discountAmount;
        this.person = person;
        this.postCode = postCode;
        this.areaCode = areaCode;
        this.tellNumber = tellNumber;
        this.address = address;
        this.fullName = fullName;
        this.companyName = companyName;
        this.economicCode = economicCode;
        this.nationalCode = nationalCode;
        this.sellerTypeCode = sellerTypeCode;
        this.sellerTypeName = sellerTypeName;
        this.stateCode = stateCode;
        this.stateName = stateName;
        this.cityCode = cityCode;
        this.cityName = cityName;
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.productCurrencyAmount = productCurrencyAmount;
        this.addedValueCurrencyAmount = addedValueCurrencyAmount;
        this.taxCurrencyAmount = taxCurrencyAmount;
        this.otherTaxCurrencyAmount = otherTaxCurrencyAmount;
        this.discountCurrencyAmount = discountCurrencyAmount;
        this.productExchangeRate = productExchangeRate;
        this.discountExchangeRate = discountExchangeRate;
        this.productRialAmount = productRialAmount;
        this.discountRialAmount = discountRialAmount;
        this.billCode = billCode;
        this.billDate = billDate;
        this.accountingDocumentCode = accountingDocumentCode;
        this.accountingDocumentDate = accountingDocumentDate;
        this.isSent = isSent;
        this.addedValueExchangeRate = addedValueExchangeRate;
        this.taxExchangeRate = taxExchangeRate;
        this.otherTaxExchangeRate = otherTaxExchangeRate;
        this.addedValueRialAmount = addedValueRialAmount;
        this.taxRialAmount = taxRialAmount;
        this.otherTaxRialAmount = otherTaxRialAmount;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getSarjam() {
        return sarjam;
    }

    public void setSarjam(Boolean sarjam) {
        this.sarjam = sarjam;
    }

    public Boolean getAzTarigheHagholAmalkari() {
        return azTarigheHagholAmalkari;
    }

    public void setAzTarigheHagholAmalkari(Boolean azTarigheHagholAmalkari) {
        this.azTarigheHagholAmalkari = azTarigheHagholAmalkari;
    }

    public String getProductTypeCode() {
        return productTypeCode;
    }

    public void setProductTypeCode(String productTypeCode) {
        this.productTypeCode = productTypeCode;
    }

    public String getProductTypeName() {
        return productTypeName;
    }

    public void setProductTypeName(String productTypeName) {
        this.productTypeName = productTypeName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Boolean getBargashti() {
        return bargashti;
    }

    public void setBargashti(Boolean bargashti) {
        this.bargashti = bargashti;
    }

    public BigDecimal getProductAmount() {
        return productAmount;
    }

    public void setProductAmount(BigDecimal productAmount) {
        this.productAmount = productAmount;
    }

    public BigDecimal getAddedValueAmount() {
        return addedValueAmount;
    }

    public void setAddedValueAmount(BigDecimal addedValueAmount) {
        this.addedValueAmount = addedValueAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getOtherTaxAmount() {
        return otherTaxAmount;
    }

    public void setOtherTaxAmount(BigDecimal otherTaxAmount) {
        this.otherTaxAmount = otherTaxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getTellNumber() {
        return tellNumber;
    }

    public void setTellNumber(String tellNumber) {
        this.tellNumber = tellNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEconomicCode() {
        return economicCode;
    }

    public void setEconomicCode(String economicCode) {
        this.economicCode = economicCode;
    }

    public String getNationalCode() {
        return nationalCode;
    }

    public void setNationalCode(String nationalCode) {
        this.nationalCode = nationalCode;
    }

    public String getSellerTypeCode() {
        return sellerTypeCode;
    }

    public void setSellerTypeCode(String sellerTypeCode) {
        this.sellerTypeCode = sellerTypeCode;
    }

    public String getSellerTypeName() {
        return sellerTypeName;
    }

    public void setSellerTypeName(String sellerTypeName) {
        this.sellerTypeName = sellerTypeName;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public BigDecimal getProductCurrencyAmount() {
        return productCurrencyAmount;
    }

    public void setProductCurrencyAmount(BigDecimal productCurrencyAmount) {
        this.productCurrencyAmount = productCurrencyAmount;
    }

    public BigDecimal getAddedValueCurrencyAmount() {
        return addedValueCurrencyAmount;
    }

    public void setAddedValueCurrencyAmount(BigDecimal addedValueCurrencyAmount) {
        this.addedValueCurrencyAmount = addedValueCurrencyAmount;
    }

    public BigDecimal getTaxCurrencyAmount() {
        return taxCurrencyAmount;
    }

    public void setTaxCurrencyAmount(BigDecimal taxCurrencyAmount) {
        this.taxCurrencyAmount = taxCurrencyAmount;
    }

    public BigDecimal getOtherTaxCurrencyAmount() {
        return otherTaxCurrencyAmount;
    }

    public void setOtherTaxCurrencyAmount(BigDecimal otherTaxCurrencyAmount) {
        this.otherTaxCurrencyAmount = otherTaxCurrencyAmount;
    }

    public BigDecimal getDiscountCurrencyAmount() {
        return discountCurrencyAmount;
    }

    public void setDiscountCurrencyAmount(BigDecimal discountCurrencyAmount) {
        this.discountCurrencyAmount = discountCurrencyAmount;
    }

    public BigDecimal getProductExchangeRate() {
        return productExchangeRate;
    }

    public void setProductExchangeRate(BigDecimal productExchangeRate) {
        this.productExchangeRate = productExchangeRate;
    }

    public BigDecimal getDiscountExchangeRate() {
        return discountExchangeRate;
    }

    public void setDiscountExchangeRate(BigDecimal discountExchangeRate) {
        this.discountExchangeRate = discountExchangeRate;
    }

    public BigDecimal getProductRialAmount() {
        return productRialAmount;
    }

    public void setProductRialAmount(BigDecimal productRialAmount) {
        this.productRialAmount = productRialAmount;
    }

    public BigDecimal getDiscountRialAmount() {
        return discountRialAmount;
    }

    public void setDiscountRialAmount(BigDecimal discountRialAmount) {
        this.discountRialAmount = discountRialAmount;
    }

    public String getBillCode() {
        return billCode;
    }

    public void setBillCode(String billCode) {
        this.billCode = billCode;
    }

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }

    public String getAccountingDocumentCode() {
        return accountingDocumentCode;
    }

    public void setAccountingDocumentCode(String accountingDocumentCode) {
        this.accountingDocumentCode = accountingDocumentCode;
    }

    public Date getAccountingDocumentDate() {
        return accountingDocumentDate;
    }

    public void setAccountingDocumentDate(Date accountingDocumentDate) {
        this.accountingDocumentDate = accountingDocumentDate;
    }

    public Boolean getSent() {
        return isSent;
    }

    public void setSent(Boolean sent) {
        isSent = sent;
    }

    public BigDecimal getAddedValueExchangeRate() {
        return addedValueExchangeRate;
    }

    public void setAddedValueExchangeRate(BigDecimal addedValueExchangeRate) {
        this.addedValueExchangeRate = addedValueExchangeRate;
    }

    public BigDecimal getTaxExchangeRate() {
        return taxExchangeRate;
    }

    public void setTaxExchangeRate(BigDecimal taxExchangeRate) {
        this.taxExchangeRate = taxExchangeRate;
    }

    public BigDecimal getOtherTaxExchangeRate() {
        return otherTaxExchangeRate;
    }

    public void setOtherTaxExchangeRate(BigDecimal otherTaxExchangeRate) {
        this.otherTaxExchangeRate = otherTaxExchangeRate;
    }

    public BigDecimal getAddedValueRialAmount() {
        return addedValueRialAmount;
    }

    public void setAddedValueRialAmount(BigDecimal addedValueRialAmount) {
        this.addedValueRialAmount = addedValueRialAmount;
    }

    public BigDecimal getTaxRialAmount() {
        return taxRialAmount;
    }

    public void setTaxRialAmount(BigDecimal taxRialAmount) {
        this.taxRialAmount = taxRialAmount;
    }

    public BigDecimal getOtherTaxRialAmount() {
        return otherTaxRialAmount;
    }

    public void setOtherTaxRialAmount(BigDecimal otherTaxRialAmount) {
        this.otherTaxRialAmount = otherTaxRialAmount;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
