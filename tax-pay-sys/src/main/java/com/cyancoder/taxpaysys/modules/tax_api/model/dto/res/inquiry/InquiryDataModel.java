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

    public String referenceNumber;
    public String uid;
    public InquiryInnerDataModel data;
    public String packetType;
    public String fiscalId;
    public String sign;
    public String status;


}
