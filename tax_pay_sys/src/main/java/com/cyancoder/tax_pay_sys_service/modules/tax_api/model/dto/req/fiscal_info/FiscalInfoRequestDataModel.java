package com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.fiscal_info;

import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.DataModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@JsonFormat
public class FiscalInfoRequestDataModel extends DataModel {



}
