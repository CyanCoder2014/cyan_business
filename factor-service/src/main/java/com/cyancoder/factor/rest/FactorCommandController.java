package com.cyancoder.factor.rest;


import com.cyancoder.factor.command.CreateFactorCommand;
import com.cyancoder.factor.model.request.CreateFactorReqModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/v2/api/factor-service/factors")
@RequiredArgsConstructor
@Slf4j
public class FactorCommandController {


    private  final Environment env;
    private  final CommandGateway commandGateway;


    @PostMapping
    public String createFactor(@RequestBody CreateFactorReqModel createFactorReqModel) throws ParseException {

        String factorDateStr = createFactorReqModel.getFactorDate();   // to consider
            SimpleDateFormat fromDateObj = new SimpleDateFormat("yyyy-MM-dd");
            Date actorDate = fromDateObj.parse(factorDateStr);

        CreateFactorCommand createFactorCommand = CreateFactorCommand.builder()
                .factorId(UUID.randomUUID().toString())
                .code(createFactorReqModel.getCode())
                .factorDate(actorDate)
                .payType(createFactorReqModel.getPayType())
                .state(createFactorReqModel.getState())
                .note(createFactorReqModel.getNote())
                .items(createFactorReqModel.getItems())
                .buyer(createFactorReqModel.getBuyer())
                .build();


        String response;

        try{
            response = commandGateway.sendAndWait(createFactorCommand);
            log.info("commandGateway -> sendAndWait (createFactorCommand)");

        }catch (Exception e){
            response = e.getLocalizedMessage();
        }


        return response;

    }


}
