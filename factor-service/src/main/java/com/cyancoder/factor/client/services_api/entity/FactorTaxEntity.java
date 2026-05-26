package com.cyancoder.factor.client.services_api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@JsonFormat
public class FactorTaxEntity {

    private String factorTaxId;

    private String factorId;


    private String taxApiUid;

    private String taxApiReference;

    private Date successesAt;

    private String taxApiSuccessesUid;

    private String taxApiSuccessesReference;

    private String taxApiCorrectionUid;

    private String taxApiCorrectionReference;

    private String taxApiCancellationUid;

    private String taxApiCancellationReference;

    private String taxApiState;

    private String taxApiMessage;

    private String taxApiData;

    private String clientId;

    private Date createdAt;

    private Date updatedAt;
}
