package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.inquiry;

import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ResultModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class InquiryResultModel extends ResultModel {

    public InquiryResponsePacketModel result;

}
