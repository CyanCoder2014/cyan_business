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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/api/client-service/companies")
@RequiredArgsConstructor
public class CompanyQueryController {


    private  final Environment env;
    private  final QueryGateway queryGateway;


    @GetMapping("/")
    public CompanyModel getCompany(
            @RequestParam String companyId,
            @RequestParam String nationalCode,
            @RequestParam String uniqueCode
//            @RequestBody GetCompanyReqModel getCompanyReqModel
    ){

        GetCompanyReqModel getCompanyReqModel =new GetCompanyReqModel();
        getCompanyReqModel.setCompanyId(companyId==""?null:companyId);
        getCompanyReqModel.setNationalCode(nationalCode==""?null:Long.getLong(nationalCode));
        getCompanyReqModel.setUniqueCode(uniqueCode==""?null:uniqueCode);
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
