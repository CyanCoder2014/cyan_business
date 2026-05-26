package com.cyancoder.taxpaysys.modules.tax_api.model;

import lombok.Data;
import java.util.Date;


@Data
public class FactorTaxModel {

    private String factorTaxId;

    private String factorId;

    private String taxApiUid;

    private String taxApiReference;

    private String taxApiState;

    private String taxApiMessage;

    private String taxApiData;

    private String clientId;

    private Date createdAt;

    private Date updatedAt;

}
