package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.inquiry;

import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.ResultModel;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat
public class InquiryResultModel extends ResultModel {

    public InquiryResponsePacketModel result;

}
