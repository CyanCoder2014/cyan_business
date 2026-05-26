package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.inquiry;


import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ErrorObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.List;

@Data
@JsonFormat
public class InquiryInnerDataModel {


    public List<ErrorObject> error;
    public List<ErrorObject> warning;

}
