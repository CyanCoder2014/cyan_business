package com.cyancoder.factor.rest;


import com.cyancoder.factor.command.CreateFactorCommand;
import com.cyancoder.factor.model.FactorModel;
import com.cyancoder.factor.model.request.CreateFactorReqModel;
import com.cyancoder.factor.query.FilterFactorQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
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
    public List<FactorModel> createFactor(@RequestBody CreateFactorReqModel createFactorReqModel){

        FilterFactorQuery filterFactorQuery = new FilterFactorQuery();

        List<FactorModel> factors = queryGateway.query(filterFactorQuery,
                ResponseTypes.multipleInstancesOf(FactorModel.class)).join();


        return factors;


    }


}
