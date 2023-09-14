package com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.server_info;

import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.ResultModel;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat
public class ServerInfoResultModel extends ResultModel {

    public ServerInfoResponsePacketModel result;

}
