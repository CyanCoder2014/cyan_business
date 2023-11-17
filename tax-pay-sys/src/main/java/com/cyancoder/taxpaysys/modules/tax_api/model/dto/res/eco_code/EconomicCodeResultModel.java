package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.eco_code;

import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ResultModel;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat
public class EconomicCodeResultModel extends ResultModel {

    public EconomicCodeResponsePacketModel result;

}
