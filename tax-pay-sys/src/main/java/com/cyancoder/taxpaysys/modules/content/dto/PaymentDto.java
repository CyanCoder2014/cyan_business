package com.cyancoder.taxpaysys.modules.content.dto;


public class PaymentDto {

    /**
     * IinNumber
     */
    private String iinn;

    /**
     * acceptorNumber
     */
    private String acn;

    /**
     * terminalNumber
     */
    private String trmn;

    /**
     * trackingNumber
     */
    private String trn;

    /**
     * payerCardNumber
     */
    private String pcn;

    /**
     * payerId
     */
    private String pid;

    /**
     * payDateTime
     */
    private Long pdt;

    public String getIinn() {
        return iinn;
    }

    public void setIinn(String iinn) {
        this.iinn = iinn;
    }

    public String getAcn() {
        return acn;
    }

    public void setAcn(String acn) {
        this.acn = acn;
    }

    public String getTrmn() {
        return trmn;
    }

    public void setTrmn(String trmn) {
        this.trmn = trmn;
    }

    public String getTrn() {
        return trn;
    }

    public void setTrn(String trn) {
        this.trn = trn;
    }

    public String getPcn() {
        return pcn;
    }

    public void setPcn(String pcn) {
        this.pcn = pcn;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Long getPdt() {
        return pdt;
    }

    public void setPdt(Long pdt) {
        this.pdt = pdt;
    }
}
