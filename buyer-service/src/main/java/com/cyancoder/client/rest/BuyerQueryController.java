package com.cyancoder.client.rest;


import com.cyancoder.client.model.BuyerModel;
import com.cyancoder.client.model.request.CreateBuyerReqModel;
import com.cyancoder.client.query.FilterBuyerQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/api/buyer-service/buyers")
@RequiredArgsConstructor
public class BuyerQueryController {


    private  final Environment env;
    private  final QueryGateway queryGateway;


    @GetMapping
    public List<BuyerModel> createBuyer(@RequestBody CreateBuyerReqModel createBuyerReqModel){

        FilterBuyerQuery filterBuyerQuery = new FilterBuyerQuery();

        List<BuyerModel> buyers = queryGateway.query(filterBuyerQuery,
                ResponseTypes.multipleInstancesOf(BuyerModel.class)).join();


        return buyers;


    }


}
