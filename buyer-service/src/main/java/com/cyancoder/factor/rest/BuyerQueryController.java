package com.cyancoder.factor.rest;


import com.cyancoder.factor.model.BuyerModel;
import com.cyancoder.factor.model.request.CreateBuyerReqModel;
import com.cyancoder.factor.query.FilterBuyerQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/api/factor-service/factors")
@RequiredArgsConstructor
public class BuyerQueryController {


    private  final Environment env;
    private  final QueryGateway queryGateway;


    @GetMapping
    public List<BuyerModel> createFactor(@RequestBody CreateBuyerReqModel createBuyerReqModel){

        FilterBuyerQuery filterBuyerQuery = new FilterBuyerQuery();

        List<BuyerModel> factors = queryGateway.query(filterBuyerQuery,
                ResponseTypes.multipleInstancesOf(BuyerModel.class)).join();


        return factors;


    }


}
