package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
@JsonFormat
public class ErrorObject {

    public String code;
    public String message;

}
