package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.inquiry;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.List;

@Data
@JsonFormat
public class InquiryResponsePacketModel {

    public String uid;
    public List<InquiryDataModel> data;
    public String packetType;



}
