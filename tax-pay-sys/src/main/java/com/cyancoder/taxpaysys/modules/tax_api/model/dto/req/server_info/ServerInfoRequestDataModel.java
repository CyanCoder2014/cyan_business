package com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.server_info;

import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.DataModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@JsonFormat
public class ServerInfoRequestDataModel extends DataModel {



}