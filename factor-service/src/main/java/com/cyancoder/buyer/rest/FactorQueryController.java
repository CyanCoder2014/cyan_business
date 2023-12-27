package com.cyancoder.buyer.rest;


import com.cyancoder.buyer.model.FactorModel;
import com.cyancoder.buyer.query.FilterFactorQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/api/factor-service/factors")
@RequiredArgsConstructor
public class FactorQueryController {


    private  final Environment env;
    private  final QueryGateway queryGateway;


    @GetMapping
    public List<FactorModel> createFactor(){

        FilterFactorQuery filterFactorQuery = new FilterFactorQuery();

        List<FactorModel> factors = queryGateway.query(filterFactorQuery,
                ResponseTypes.multipleInstancesOf(FactorModel.class)).join();


        return factors;


    }


}
