package com.cyancoder.buyer.rest;


import com.cyancoder.buyer.command.CreateFactorCommand;
import com.cyancoder.buyer.model.request.CreateFactorReqModel;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/api/factor-service/factors")
@RequiredArgsConstructor
public class FactorCommandController {


    private  final Environment env;
    private  final CommandGateway commandGateway;


    @PostMapping
    public String createFactor(@RequestBody CreateFactorReqModel createFactorReqModel){
        CreateFactorCommand createFactorCommand = CreateFactorCommand.builder()
//                .factorId(UUID.randomUUID().toString())
//                .factorId(createFactorReqModel.getId())
                .build();

        String response;

        try{
            response = commandGateway.sendAndWait(createFactorCommand);
        }catch (Exception e){
            response = e.getLocalizedMessage();
        }


        return response;

    }


}
