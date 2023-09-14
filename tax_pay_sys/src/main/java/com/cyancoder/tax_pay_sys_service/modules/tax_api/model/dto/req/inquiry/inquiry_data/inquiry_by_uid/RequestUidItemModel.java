package com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.inquiry.inquiry_data.inquiry_by_uid;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonFormat
public class RequestUidItemModel {


    public String uid;
    public String fiscalId;


}
