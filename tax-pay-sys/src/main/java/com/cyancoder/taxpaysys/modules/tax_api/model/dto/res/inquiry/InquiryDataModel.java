package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.inquiry;


import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ErrorObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonFormat
public class InquiryDataModel  {

    public List<ErrorObject> errors;
    public List<ErrorObject> warning;

}
