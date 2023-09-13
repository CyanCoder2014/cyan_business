package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.inquiry.inquiry_data;


import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.DataModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.inquiry.InquiryDataModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@JsonFormat
public class InquiryDataByTimeModel extends InquiryDataModel {

    public String time;

}
