package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.auth;

import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ResultModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

@JsonFormat
@Data
public class AuthResultModel extends ResultModel {

    public AuthResponsePacketModel result;

}
