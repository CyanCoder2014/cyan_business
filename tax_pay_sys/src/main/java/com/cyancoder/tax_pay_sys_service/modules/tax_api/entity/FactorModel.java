package com.cyancoder.tax_pay_sys_service.modules.tax_api.entity;


import java.io.Serializable;
import java.math.BigDecimal;


public class FactorModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean sarjam;// سر جمع
    private boolean azTarigheHagholAmalkari;// انجام معامله از طریق حق العمل کاری
    private int productType;// نوع کالا
    private String productDescription;// شرح کالا یا خدمات
    private int productCode;//شناسه کالا
    private boolean bargashti;// برگشتی
    private BigDecimal productAmount;// مبلغ کالا یا خدمت
    private BigDecimal addedValueAmount;// مالیات بر ارزش افزوده
    private BigDecimal taxAmount;// عوارض
    private BigDecimal otherTaxAmount;// سایر عوارض
    private BigDecimal discountAmount;// مبلغ تخفیف
    private int person;// عدد نوع فروشنده با توجه به توضیحات
    private String postCode;// کد پستی
    private String areaCode;// پیش کد شهر
    private String tellNumber;// تلفن
    private String address;// آدرس فروشنده
    private String fullName;// نام فروشنده
    private String companyName;// نام شرکت/
    private String economicCode;// کد اقتصادی
    private String nationalCode; // کدملی/ شناسنامه
    private int sellerType;// نوع فروشنده
    private int stateCode;// کد استان
    private int cityCode;// کدشهر
    private int currency;// نوع ارز
    private BigDecimal productCurrencyAmount;// مبلغ ارزی کالا یا خدمت
    private BigDecimal addedValueCurrencyAmount;// مبلغ ارزی ارزش افزوده
    private BigDecimal taxCurrencyAmount;// مبلغ ارزی عوارض
    private BigDecimal otherTaxCurrencyAmount;// مبلغ ارزی سایر عوارض
    private BigDecimal discountCurrencyAmount;// مبلغ ارزی تخفیف
    private BigDecimal productExchangeRate;// نرخ برابری ارز مبلغ کالا یا خدمت
    private BigDecimal discountExchangeRate;// نرخ برابری ارز تخفیف
    private BigDecimal productRialAmount;// معادل ریالی مبلغ ارزی کالا یا خدمت
    private BigDecimal discountRialAmount;// معادل ریالی مبلغ ارزی تخفیف
    private String billCode;// شماره صورتحساب
    private String billDate;// تاریخ صورتحساب
    private int accountingDocumentCode;// شماره سند حسابداری
    private String accountingDocumentDate;// تاریخ صورتحساب
    private boolean isSent;// IsSent
    private BigDecimal addedValueExchangeRate;// ArzBarabari_MaliatArzeshAfzoodeh
    private BigDecimal taxExchangeRate;// ArzBarabari_AvarezArzeshAfzoodeh
    private BigDecimal otherTaxExchangeRate;// ArzBarabari_SayerAvarez
    private BigDecimal addedValueRialAmount;// MoadelRialiMaliatArzeshAfzoodeh
    private BigDecimal taxRialAmount;// MoadelRialiAvarezArzeshAfzoodeh
    private BigDecimal otherTaxRialAmount;// MoadelRialiSayerAvarez

    public FactorModel() {
    }

    public boolean isSarjam() {
        return sarjam;
    }

    public void setSarjam(boolean sarjam) {
        this.sarjam = sarjam;
    }

    public boolean isAzTarigheHagholAmalkari() {
        return azTarigheHagholAmalkari;
    }

    public void setAzTarigheHagholAmalkari(boolean azTarigheHagholAmalkari) {
        this.azTarigheHagholAmalkari = azTarigheHagholAmalkari;
    }

    public int getProductType() {
        return productType;
    }

    public void setProductType(int productType) {
        this.productType = productType;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public int getProductCode() {
        return productCode;
    }

    public void setProductCode(int productCode) {
        this.productCode = productCode;
    }

    public boolean isBargashti() {
        return bargashti;
    }

    public void setBargashti(boolean bargashti) {
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

    public int getPerson() {
        return person;
    }

    public void setPerson(int person) {
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

    public int getSellerType() {
        return sellerType;
    }

    public void setSellerType(int sellerType) {
        this.sellerType = sellerType;
    }

    public int getStateCode() {
        return stateCode;
    }

    public void setStateCode(int stateCode) {
        this.stateCode = stateCode;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
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

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
    }

    public int getAccountingDocumentCode() {
        return accountingDocumentCode;
    }

    public void setAccountingDocumentCode(int accountingDocumentCode) {
        this.accountingDocumentCode = accountingDocumentCode;
    }

    public String getAccountingDocumentDate() {
        return accountingDocumentDate;
    }

    public void setAccountingDocumentDate(String accountingDocumentDate) {
        this.accountingDocumentDate = accountingDocumentDate;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
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
}
