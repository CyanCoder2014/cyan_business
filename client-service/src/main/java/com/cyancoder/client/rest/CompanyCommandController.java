package com.cyancoder.client.rest;


import com.cyancoder.client.command.CreateClientCommand;
import com.cyancoder.client.command.CreateCompanyCommand;
import com.cyancoder.client.config.OAuthToken;
import com.cyancoder.client.model.request.CreateClientReqModel;
import com.cyancoder.client.model.request.CreateCompanyReqModel;
import com.cyancoder.client.utils.Encrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v2/api/client-service/companies")
@RequiredArgsConstructor
@Slf4j
public class CompanyCommandController {


    private  final OAuthToken oAuthToken;
    private  final CommandGateway commandGateway;


    @PostMapping
    public String createCompany(@RequestBody CreateCompanyReqModel createCompanyReqModel){


        CreateCompanyCommand  createCompanyCommand = CreateCompanyCommand.builder()
                .companyId(UUID.randomUUID().toString())
                .clientId(oAuthToken.getAttribute("client_id"))
                .name(createCompanyReqModel.getName())
                .nationalCode(createCompanyReqModel.getNationalCode())
                .economicCode(createCompanyReqModel.getEconomicCode())
                .uniqueCode(Encrypt.hash(createCompanyReqModel.getUniqueCode()))
                .pk(Encrypt.encrypt(createCompanyReqModel.getPk(),createCompanyReqModel.getUniqueCode()))
                .tell(createCompanyReqModel.getTell())
                .address(createCompanyReqModel.getAddress())
                .postCode(createCompanyReqModel.getPostCode())
                .cityId(createCompanyReqModel.getCityId())
                .build();

        String response;

        log.info("createCompanyCommand: {}",createCompanyCommand);

        try{
            response = commandGateway.sendAndWait(createCompanyCommand);
        }catch (Exception e){
            response = e.getLocalizedMessage();
        }


        return response;

    }


}
