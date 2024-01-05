package com.cyancoder.taxpaysys.modules.content.dto;


import java.math.BigDecimal;

public class InvoiceBodyDto {

    /**
     * service Stuff Id
     */
    private String sstid;

    /**
     * serviceStuffTitle
     */
    private String sstt;

    /**
     * measurementUnit
     */
    private String mu;

    /**
     * amount
     */
    private Double am;

    /**
     * fee
     */
    private BigDecimal fee;

    /**
     * currencyFee
     */
    private BigDecimal cfee;

    /**
     * currencyType
     */
    private String cut;

    /**
     * exchangeRate
     */
    private String exr;

    /**
     * preDiscount
     */
    private BigDecimal prdis;

    /**
     * discount
     */
    private BigDecimal dis;

    /**
     * afterDiscount
     */
    private BigDecimal adis;

    /**
     * vatRate
     */
    private BigDecimal vra;

    /**
     * vatAmount
     */
    private BigDecimal vam;

    /**
     * overDutyTitle
     */
    private String odt;

    /**
     * overDutyRate
     */
    private BigDecimal odr;

    /**
     * overDutyAmount
     */
    private BigDecimal odam;

    /**
     * otherLegalTitle
     */
    private String olt;

    /**
     * otherLegalRate
     */
    private BigDecimal olr;

    /**
     * otherLegalAmount
     */
    private BigDecimal olam;

    /**
     * constructionFee
     */
    private BigDecimal consfee;

    /**
     * sellerProfit
     */
    private BigDecimal spro;

    /**
     * brokerSalary
     */
    private BigDecimal bros;

    /**
     * totalConstructionProfitBrokerSalary
     */
    private BigDecimal tcpbs;

    /**
     * cashOfPayment
     */
    private BigDecimal cop;

    /**
     * buyerSRegisterNumber
     */
    private String bsrn;

    /**
     * vatOfPayment
     */
    private String vop;

    /**
     * totalServiceStuffAmount
     */
    private BigDecimal tsstam;


    public String getSstid() {
        return sstid;
    }

    public void setSstid(String sstid) {
        this.sstid = sstid;
    }

    public String getSstt() {
        return sstt;
    }

    public void setSstt(String sstt) {
        this.sstt = sstt;
    }

    public String getMu() {
        return mu;
    }

    public void setMu(String mu) {
        this.mu = mu;
    }

    public Double getAm() {
        return am;
    }

    public void setAm(Double am) {
        this.am = am;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public BigDecimal getCfee() {
        return cfee;
    }

    public void setCfee(BigDecimal cfee) {
        this.cfee = cfee;
    }

    public String getCut() {
        return cut;
    }

    public void setCut(String cut) {
        this.cut = cut;
    }

    public String getExr() {
        return exr;
    }

    public void setExr(String exr) {
        this.exr = exr;
    }

    public BigDecimal getPrdis() {
        return prdis;
    }

    public void setPrdis(BigDecimal prdis) {
        this.prdis = prdis;
    }

    public BigDecimal getDis() {
        return dis;
    }

    public void setDis(BigDecimal dis) {
        this.dis = dis;
    }

    public BigDecimal getAdis() {
        return adis;
    }

    public void setAdis(BigDecimal adis) {
        this.adis = adis;
    }

    public BigDecimal getVra() {
        return vra;
    }

    public void setVra(BigDecimal vra) {
        this.vra = vra;
    }

    public BigDecimal getVam() {
        return vam;
    }

    public void setVam(BigDecimal vam) {
        this.vam = vam;
    }

    public String getOdt() {
        return odt;
    }

    public void setOdt(String odt) {
        this.odt = odt;
    }

    public BigDecimal getOdr() {
        return odr;
    }

    public void setOdr(BigDecimal odr) {
        this.odr = odr;
    }

    public BigDecimal getOdam() {
        return odam;
    }

    public void setOdam(BigDecimal odam) {
        this.odam = odam;
    }

    public String getOlt() {
        return olt;
    }

    public void setOlt(String olt) {
        this.olt = olt;
    }

    public BigDecimal getOlr() {
        return olr;
    }

    public void setOlr(BigDecimal olr) {
        this.olr = olr;
    }

    public BigDecimal getOlam() {
        return olam;
    }

    public void setOlam(BigDecimal olam) {
        this.olam = olam;
    }

    public BigDecimal getConsfee() {
        return consfee;
    }

    public void setConsfee(BigDecimal consfee) {
        this.consfee = consfee;
    }

    public BigDecimal getSpro() {
        return spro;
    }

    public void setSpro(BigDecimal spro) {
        this.spro = spro;
    }

    public BigDecimal getBros() {
        return bros;
    }

    public void setBros(BigDecimal bros) {
        this.bros = bros;
    }

    public BigDecimal getTcpbs() {
        return tcpbs;
    }

    public void setTcpbs(BigDecimal tcpbs) {
        this.tcpbs = tcpbs;
    }

    public BigDecimal getCop() {
        return cop;
    }

    public void setCop(BigDecimal cop) {
        this.cop = cop;
    }

    public String getBsrn() {
        return bsrn;
    }

    public void setBsrn(String bsrn) {
        this.bsrn = bsrn;
    }

    public String getVop() {
        return vop;
    }

    public void setVop(String vop) {
        this.vop = vop;
    }

    public BigDecimal getTsstam() {
        return tsstam;
    }

    public void setTsstam(BigDecimal tsstam) {
        this.tsstam = tsstam;
    }
}
