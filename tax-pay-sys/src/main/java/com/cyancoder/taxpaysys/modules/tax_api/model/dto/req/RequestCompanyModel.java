package com.cyancoder.taxpaysys.modules.tax_api.model.dto.req;


import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.taxpaysys.modules.tax_api.model.Header;
import com.cyancoder.taxpaysys.util.CryptoUtils;
import com.cyancoder.taxpaysys.util.KeyUtil;
import com.cyancoder.taxpaysys.util.SignText;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@Data
@JsonFormat
@NoArgsConstructor
public class RequestCompanyModel {

    public RequestCompanyModel(String companyId){
        this.companyId = companyId;

    }

    private String companyId;
    private Long nationalCode;
    private String uniqueCode;


}