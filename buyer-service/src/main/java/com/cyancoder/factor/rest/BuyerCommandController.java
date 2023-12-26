package com.cyancoder.factor.rest;


import com.cyancoder.factor.command.CreateBuyerCommand;
import com.cyancoder.factor.model.request.CreateBuyerReqModel;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/api/factor-service/factors")
@RequiredArgsConstructor
public class BuyerCommandController {


    private  final Environment env;
    private  final CommandGateway commandGateway;


    @PostMapping
    public String createFactor(@RequestBody CreateBuyerReqModel createBuyerReqModel){
        CreateBuyerCommand createBuyerCommand = CreateBuyerCommand.builder()
//                .factorId(UUID.randomUUID().toString())
//                .factorId(createFactorReqModel.getId())
                .build();

        String response;

        try{
            response = commandGateway.sendAndWait(createBuyerCommand);
        }catch (Exception e){
            response = e.getLocalizedMessage();
        }


        return response;

    }


}
