package com.cyancoder.product.rest;


import com.cyancoder.product.command.CreateClientCommand;
import com.cyancoder.product.model.request.CreateClientReqModel;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/api/client-service/clients")
@RequiredArgsConstructor
public class ClientCommandController {


    private  final Environment env;
    private  final CommandGateway commandGateway;


    @PostMapping
    public String createClient(@RequestBody CreateClientReqModel createClientReqModel){
        CreateClientCommand createClientCommand = CreateClientCommand.builder()
//                .clientId(UUID.randomUUID().toString())
//                .clientId(createClientReqModel.getId())
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
