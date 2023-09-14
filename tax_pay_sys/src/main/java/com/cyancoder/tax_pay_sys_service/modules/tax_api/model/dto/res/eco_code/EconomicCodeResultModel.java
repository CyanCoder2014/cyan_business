package com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.eco_code;

import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.ResultModel;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat
public class EconomicCodeResultModel extends ResultModel {

    public EconomicCodeResponsePacketModel result;

}
