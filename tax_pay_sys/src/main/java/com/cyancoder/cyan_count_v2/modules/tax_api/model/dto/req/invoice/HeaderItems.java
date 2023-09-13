package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.invoice;

import com.fasterxml.jackson.annotation.JsonFormat;
import jdk.jfr.Timestamp;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@JsonFormat
public class HeaderItems {



    private String taxid;
    @Timestamp
    private String indatim;
    @Timestamp
    private String Indati2m;
    private int inty;
    private String inno;
    private String irtaxid;
    private int inp;
    private int ins;
    private int tins;
    private int tob;
    private int bid;
    private int tinb;
    private int sbc;
    private int bpc;
    private int bbc;
    private int ft;
    private String bpn;
    private int scln;
    private int scc;
    private int crn;
    private int billid;
    private double tprdis;
    private double tdis;
    private double tadis;
    private double tvam;
    private double todam;
    private double tbill;
    private int setm;
    private double cap;
    private double insp;
    private double tvop;
    private int dpvb;
    private double Tax17;

}
