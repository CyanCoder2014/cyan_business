package com.cyancoder.taxpaysys.modules.tax_api.entity;


import java.util.Date;

public class FactorReportModel {

    private static final long serialVersionUID = 1L;

    private long[] sellers;

    private Date dateFrom;

    private Date dateUntil;

    private boolean buyDetail;

    private boolean sellDetail;

    public FactorReportModel() {
    }

    public long[] getSellers() {
        return sellers;
    }

    public void setSellers(long[] sellers) {
        this.sellers = sellers;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateUntil() {
        return dateUntil;
    }

    public void setDateUntil(Date dateUntil) {
        this.dateUntil = dateUntil;
    }

    public boolean isBuyDetail() {
        return buyDetail;
    }

    public void setBuyDetail(boolean buyDetail) {
        this.buyDetail = buyDetail;
    }

    public boolean isSellDetail() {
        return sellDetail;
    }

    public void setSellDetail(boolean sellDetail) {
        this.sellDetail = sellDetail;
    }
}
