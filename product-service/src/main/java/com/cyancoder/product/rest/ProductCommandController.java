package com.cyancoder.product.rest;


import com.cyancoder.product.command.CreateProductCommand;
import com.cyancoder.product.model.request.CreateProductReqModel;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/api/product-service/products")
@RequiredArgsConstructor
public class ProductCommandController {


    private  final Environment env;
    private  final CommandGateway commandGateway;


    @PostMapping
    public String createProduct(@RequestBody CreateProductReqModel createProductReqModel){
        CreateProductCommand createProductCommand = CreateProductCommand.builder()
//                .productId(UUID.randomUUID().toString())
//                .productId(createProductReqModel.getId())
                .build();

        String response;

        try{
            response = commandGateway.sendAndWait(createProductCommand);
        }catch (Exception e){
            response = e.getLocalizedMessage();
        }


        return response;

    }


}
