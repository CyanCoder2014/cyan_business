package com.cyancoder.client.rest;


import com.cyancoder.client.command.CreateClientCommand;
import com.cyancoder.client.model.request.CreateClientReqModel;
import com.cyancoder.client.utils.Encrypt;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v2/api/client-service/clients")
@RequiredArgsConstructor
public class ClientCommandController {


    private  final Environment env;
    private  final CommandGateway commandGateway;


    @PostMapping
    public String createClient(@RequestBody CreateClientReqModel createClientReqModel){
        CreateClientCommand createClientCommand = CreateClientCommand.builder()
                .clientId(UUID.randomUUID().toString())
                .name(createClientReqModel.getName())
                .nationalCode(createClientReqModel.getNationalCode())
                .economicCode(createClientReqModel.getEconomicCode())
                .uniqueCode(Encrypt.hash(createClientReqModel.getUniqueCode()))
                .pk(Encrypt.encrypt(createClientReqModel.getPk(),createClientReqModel.getUniqueCode()))
                .tell(createClientReqModel.getTell())
                .address(createClientReqModel.getAddress())
                .postCode(createClientReqModel.getPostCode())
                .cityId(createClientReqModel.getCityId())
                .build();

        String response;

        try{
            response = commandGateway.sendAndWait(createClientCommand);
        }catch (Exception e){
            response = e.getLocalizedMessage();
        }


        return response;

    }


}
