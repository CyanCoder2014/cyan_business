package com.cyancoder.product.rest;


import com.cyancoder.product.command.CreateBuyerCommand;
import com.cyancoder.product.model.request.CreateBuyerReqModel;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/api/buyer-service/buyers")
@RequiredArgsConstructor
public class BuyerCommandController {


    private  final Environment env;
    private  final CommandGateway commandGateway;


    @PostMapping
    public String createBuyer(@RequestBody CreateBuyerReqModel createBuyerReqModel){
        CreateBuyerCommand createBuyerCommand = CreateBuyerCommand.builder()
//                .buyerId(UUID.randomUUID().toString())
//                .buyerId(createBuyerReqModel.getId())
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
