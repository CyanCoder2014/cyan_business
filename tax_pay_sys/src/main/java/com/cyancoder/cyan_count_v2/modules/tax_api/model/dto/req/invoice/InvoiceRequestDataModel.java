package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.invoice;


import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.DataModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import jdk.jfr.Timestamp;
import lombok.Builder;
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



