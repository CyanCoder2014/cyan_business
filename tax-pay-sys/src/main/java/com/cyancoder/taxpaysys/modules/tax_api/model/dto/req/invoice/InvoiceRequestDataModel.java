package com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.invoice;


import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.DataModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonFormat
public class InvoiceRequestDataModel extends DataModel {


    public InvoiceRequestDataModel(HeaderItems headerItems,BodyItems bodyItems){
        this.header = headerItems;
        this.body = bodyItems;
    }

    private HeaderItems header;
    private BodyItems body;





}



