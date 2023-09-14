package com.cyancoder.tax_pay_sys_service.modules.home.dto.res;

import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.Header;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.RequestModel;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat
public class TestResponseModel {

//    public String requestTraceId;
//    public String timeStamp;
    public Header header;

    public RequestModel body;
    public String normalJsonStr;
    public String signatureStr;

    public TestResponseModel(Header header,
                      RequestModel body,
                      String normalJsonStr,
                      String signatureStr){
//        this.requestTraceId = requestTraceId;
//        this.timeStamp = timeStamp;
        this.header = header;
        this.body = body;
        this.normalJsonStr = normalJsonStr;
        this.signatureStr = signatureStr;

    }

}
