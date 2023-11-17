package com.cyancoder.taxpaysys.modules.content.dto;


import java.math.BigDecimal;

public class InvoiceHeaderDto {

    /**
     * invoiceDateTimeGregorian
     */
    private Long indati2m;

    /**
     * invoiceDateTime
     */
    private Long indatim;

    /**
     * invoiceType
     */
    private Integer inty;

    /**
     * flightType
     */
    private Integer ft;

    /**
     * invoiceNumber
     */
    private String inno;

    /**
     * invoiceReferenceTaxId
     */
    private String irtaxid;

    /**
     * sellerCustomsLicenceNumber
     */
    private Long scln;

    /**
     * settlementType
     */
    private Integer setm;

    /**
     * sellerTaxIdentificationNumber
     */
    private String tins;

    /**
     * cashPayment
     */
    private BigDecimal cap;

    /**
     * buyerId
     */
    private String bid;

    /**
     * installmentPayment
     */
    private BigDecimal insp;

    /**
     * totalVatOfPayment
     */
    private BigDecimal tvop;

    /**
     * buyerPostalCode
     */
    private String bpc;

    /**
     * buyerVatPaymentStatus
     */
//    private Integer dpvb;

    /**
     * tax17
     */
    private BigDecimal tax17;

    /**
     * taxId
     */
    private String taxid;

    /**
     * invoicePattern
     */
    private Integer inp;

    /**
     * sellerCustomsCode
     */
    private String scc;

    /**
     * invoiceSubject
     */
    private Integer ins;

    /**
     * billingId
     */
    private String billid;

    /**
     * totalPreDiscount
     */
    private BigDecimal tprdis;

    /**
     * totalDiscount
     */
    private BigDecimal tdis;

    /**
     * totalAfterDiscount
     */
    private BigDecimal tadis;

    /**
     * totalVatAmount
     */
    private BigDecimal tvam;

    /**
     * totalOtherDutyAmount
     */
    private BigDecimal todam;

    /**
     * totalBill
     */
    private BigDecimal tbill;

    /**
     * typeOfBuyer
     */
    private Integer tob;

    /**
     * buyerTaxIdentificationNumber
     */
    private String tinb;

    /**
     * sellerBranchCode
     */
    private String sbc;

    /**
     * buyerBranchCode
     */
    private String bbc;

    /**
     * buyerPassportNumber
     */
    private String bpn;

    /**
     * contractRegistrationNumber
     */
    private Integer crn;


    public Long getIndati2m() {
        return indati2m;
    }

    public void setIndati2m(Long indati2m) {
        this.indati2m = indati2m;
    }

    public Long getIndatim() {
        return indatim;
    }

    public void setIndatim(Long indatim) {
        this.indatim = indatim;
    }

    public Integer getInty() {
        return inty;
    }

    public void setInty(Integer inty) {
        this.inty = inty;
    }

    public Integer getFt() {
        return ft;
    }

    public void setFt(Integer ft) {
        this.ft = ft;
    }

    public String getInno() {
        return inno;
    }

    public void setInno(String inno) {
        this.inno = inno;
    }

    public String getIrtaxid() {
        return irtaxid;
    }

    public void setIrtaxid(String irtaxid) {
        this.irtaxid = irtaxid;
    }

    public Long getScln() {
        return scln;
    }

    public void setScln(Long scln) {
        this.scln = scln;
    }

    public Integer getSetm() {
        return setm;
    }

    public void setSetm(Integer setm) {
        this.setm = setm;
    }

    public String getTins() {
        return tins;
    }

    public void setTins(String tins) {
        this.tins = tins;
    }

    public BigDecimal getCap() {
        return cap;
    }

    public void setCap(BigDecimal cap) {
        this.cap = cap;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public BigDecimal getInsp() {
        return insp;
    }

    public void setInsp(BigDecimal insp) {
        this.insp = insp;
    }

    public BigDecimal getTvop() {
        return tvop;
    }

    public void setTvop(BigDecimal tvop) {
        this.tvop = tvop;
    }

    public String getBpc() {
        return bpc;
    }

    public void setBpc(String bpc) {
        this.bpc = bpc;
    }

//    public Integer getDpvb() {
//        return dpvb;
//    }

//    public void setDpvb(Integer dpvb) {
//        this.dpvb = dpvb;
//    }

    public BigDecimal getTax17() {
        return tax17;
    }

    public void setTax17(BigDecimal tax17) {
        this.tax17 = tax17;
    }

    public String getTaxid() {
        return taxid;
    }

    public void setTaxid(String taxid) {
        this.taxid = taxid;
    }

    public Integer getInp() {
        return inp;
    }

    public void setInp(Integer inp) {
        this.inp = inp;
    }

    public String getScc() {
        return scc;
    }

    public void setScc(String scc) {
        this.scc = scc;
    }

    public Integer getIns() {
        return ins;
    }

    public void setIns(Integer ins) {
        this.ins = ins;
    }

    public String getBillid() {
        return billid;
    }

    public void setBillid(String billid) {
        this.billid = billid;
    }

    public BigDecimal getTprdis() {
        return tprdis;
    }

    public void setTprdis(BigDecimal tprdis) {
        this.tprdis = tprdis;
    }

    public BigDecimal getTdis() {
        return tdis;
    }

    public void setTdis(BigDecimal tdis) {
        this.tdis = tdis;
    }

    public BigDecimal getTadis() {
        return tadis;
    }

    public void setTadis(BigDecimal tadis) {
        this.tadis = tadis;
    }

    public BigDecimal getTvam() {
        return tvam;
    }

    public void setTvam(BigDecimal tvam) {
        this.tvam = tvam;
    }

    public BigDecimal getTodam() {
        return todam;
    }

    public void setTodam(BigDecimal todam) {
        this.todam = todam;
    }

    public BigDecimal getTbill() {
        return tbill;
    }

    public void setTbill(BigDecimal tbill) {
        this.tbill = tbill;
    }

    public Integer getTob() {
        return tob;
    }

    public void setTob(Integer tob) {
        this.tob = tob;
    }

    public String getTinb() {
        return tinb;
    }

    public void setTinb(String tinb) {
        this.tinb = tinb;
    }

    public String getSbc() {
        return sbc;
    }

    public void setSbc(String sbc) {
        this.sbc = sbc;
    }

    public String getBbc() {
        return bbc;
    }

    public void setBbc(String bbc) {
        this.bbc = bbc;
    }

    public String getBpn() {
        return bpn;
    }

    public void setBpn(String bpn) {
        this.bpn = bpn;
    }

    public Integer getCrn() {
        return crn;
    }

    public void setCrn(Integer crn) {
        this.crn = crn;
    }
}
