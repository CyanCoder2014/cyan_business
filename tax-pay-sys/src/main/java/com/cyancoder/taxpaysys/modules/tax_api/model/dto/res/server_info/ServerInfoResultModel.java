package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.server_info;

import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ResultModel;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat
public class ServerInfoResultModel extends ResultModel {

    public ServerInfoResponsePacketModel result;

}
