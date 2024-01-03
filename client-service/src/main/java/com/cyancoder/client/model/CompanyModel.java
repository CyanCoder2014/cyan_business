package com.cyancoder.client.model;


import com.cyancoder.client.entity.ClientEntity;
import com.cyancoder.client.entity.CompanyEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyModel {

    public CompanyModel(CompanyEntity companyEntity) {
        BeanUtils.copyProperties(companyEntity, this);
    }

    private String companyId;

    private String clientId;
    private String name;
    private Long nationalCode;
    private String economicCode;
    private String uniqueCode;
    private String pk;
    private String legalType;
    private String tell;
    private String address;
    private String postCode;
    private String cityId;
}
