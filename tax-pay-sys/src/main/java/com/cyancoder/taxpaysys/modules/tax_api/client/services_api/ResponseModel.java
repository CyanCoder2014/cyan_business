package com.cyancoder.taxpaysys.modules.tax_api.client.services_api;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonFormat
public class ResponseModel<Factor> extends ArrayList {


    public ResponseModel(Object object){}

}
