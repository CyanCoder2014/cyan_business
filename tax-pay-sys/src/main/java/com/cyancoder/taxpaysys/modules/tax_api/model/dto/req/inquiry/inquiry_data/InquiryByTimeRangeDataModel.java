package com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.inquiry.inquiry_data;


import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.inquiry.InquiryDataModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@JsonFormat
public class InquiryByTimeRangeDataModel extends InquiryDataModel {

    public String startDate;
    public String endDate;

}
