package com.cyancoder.client.rest;


import com.cyancoder.client.model.ClientModel;
import com.cyancoder.client.model.CompanyModel;
import com.cyancoder.client.model.request.CreateClientReqModel;
import com.cyancoder.client.model.request.CreateCompanyReqModel;
import com.cyancoder.client.model.request.GetCompanyReqModel;
import com.cyancoder.client.query.FilterClientQuery;
import com.cyancoder.client.query.FilterCompanyQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v2/api/client-service/companies")
@RequiredArgsConstructor
public class CompanyQueryController {


    private  final Environment env;
    private  final QueryGateway queryGateway;


    @GetMapping("/")
    public CompanyModel getCompany(@RequestBody GetCompanyReqModel getCompanyReqModel){

        FilterCompanyQuery filterCompanyQuery = new FilterCompanyQuery(getCompanyReqModel);

        CompanyModel companies = queryGateway.query(filterCompanyQuery,
                ResponseTypes.instanceOf(CompanyModel.class)).join();


        return companies;


    }

    @GetMapping
    public List<CompanyModel> getCompanies(@RequestBody GetCompanyReqModel getCompanyReqModel){

        FilterCompanyQuery filterCompanyQuery = new FilterCompanyQuery(getCompanyReqModel);

        List<CompanyModel> companies = queryGateway.query(filterCompanyQuery,
                ResponseTypes.multipleInstancesOf(CompanyModel.class)).join();


        return companies;


    }


}
