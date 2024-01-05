package com.cyancoder.client.query;

import com.cyancoder.client.model.request.GetCompanyReqModel;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class FilterCompanyQuery {

    private String companyId;
    private Long nationalCode;
    private String uniqueCode;
    public FilterCompanyQuery(GetCompanyReqModel getCompanyReqModel){
        BeanUtils.copyProperties(getCompanyReqModel, this);
    }

}
