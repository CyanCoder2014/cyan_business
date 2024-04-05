package com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.inquiry.inquiry_data.inquiry_by_uid;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@JsonFormat
public class InquiryByUidDataModel extends ArrayList<RequestUidItemModel> {


}


