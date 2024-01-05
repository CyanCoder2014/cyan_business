package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.auth;

import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ResultModel;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat
public class AuthResultModel extends ResultModel {

    public AuthResponsePacketModel result;

}
