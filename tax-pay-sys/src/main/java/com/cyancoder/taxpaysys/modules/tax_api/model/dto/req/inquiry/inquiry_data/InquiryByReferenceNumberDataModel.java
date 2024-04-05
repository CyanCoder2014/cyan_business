package com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.inquiry.inquiry_data;


import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.inquiry.InquiryDataModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@JsonFormat
public class InquiryByReferenceNumberDataModel extends InquiryDataModel {

    public List<String> referenceNumber;

}
