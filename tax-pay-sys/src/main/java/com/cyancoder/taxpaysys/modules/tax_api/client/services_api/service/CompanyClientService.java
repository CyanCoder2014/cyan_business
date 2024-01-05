package com.cyancoder.taxpaysys.modules.tax_api.client.services_api.service;

import com.cyancoder.taxpaysys.config.OauthToken;
import com.cyancoder.taxpaysys.modules.tax_api.client.services_api.rest.CompanyClient;
import com.cyancoder.taxpaysys.modules.tax_api.model.CompanyModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestCompanyModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyClientService {

    private final CompanyClient companyClient;
    private final OauthToken oauthToken;
//    private final WebClient.Builder webClientBuilder;

    public CompanyModel getCompany(String companyId)  {

//        new RequestCompanyModel(companyId)
        return companyClient.getItem("Bearer "+oauthToken.getToken(),
                companyId,"",""
                );
    }


}
