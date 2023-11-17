package com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.invoice;

import com.fasterxml.jackson.annotation.JsonFormat;
import jdk.jfr.Timestamp;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@JsonFormat
public class BodyItems {

    private int sstid;
    private String sstt;
    private int mu;
    private int am;
    private double fee;
    private double cfee;
    private String cut;
    private double exr;
    private double prdis;
    private double dis;
    private double adis;
    private double vra;
    private double vam;
    private String odt;
    private double odr;
    private double odam;
    private String olt;
    private double olr;
    private double olam;
    private double consfee;
    private double spro;
    private double bros;
    private double tcpbs;
    private double cop;
    private double vop;
    private int bsrn;
    private double tsstam;
    private int iinn;
    private int acn;
    private int trmn;
    private int trn;
    private int pcn;
    private int pid;
    @Timestamp
    private String pdt;



}
