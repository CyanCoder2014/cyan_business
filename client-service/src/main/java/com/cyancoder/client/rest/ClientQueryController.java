package com.cyancoder.client.rest;


import com.cyancoder.client.model.ClientModel;
import com.cyancoder.client.model.request.CreateClientReqModel;
import com.cyancoder.client.query.FilterClientQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/api/client-service/clients")
@RequiredArgsConstructor
public class ClientQueryController {


    private  final Environment env;
    private  final QueryGateway queryGateway;


    @GetMapping
    public List<ClientModel> createClient(@RequestBody CreateClientReqModel createClientReqModel){

        FilterClientQuery filterClientQuery = new FilterClientQuery();

        List<ClientModel> clients = queryGateway.query(filterClientQuery,
                ResponseTypes.multipleInstancesOf(ClientModel.class)).join();


        return clients;


    }


}
