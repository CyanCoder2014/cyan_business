package com.cyancoder.taxpaysys.modules.tax_api.client.services_api.service;

import com.cyancoder.taxpaysys.modules.tax_api.client.services_api.ResponseModel;
import com.cyancoder.taxpaysys.modules.tax_api.client.services_api.rest.FactorClient;
import com.cyancoder.taxpaysys.modules.tax_api.model.FactorModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactorClientService {

    private final FactorClient factorClient;
    private final WebClient.Builder webClientBuilder;

    public Object getFactors()  {


        Object res = webClientBuilder.build().get()
                .uri("http://factor-service/v2/api/factors"
//                        , uriBuilder -> uriBuilder.queryParam("test", 222).build()
                )
                .retrieve()
                .bodyToMono(Object.class)
                .block();



        return res;
//        return factorClient.getItems();

//        try {
//            return new ResponseModel(factorClient.getItems());
//        } catch (Exception e) {
//            throw e;////////////////////
//        }
    }


    public List<FactorModel> findByCodeBetween(String codeFrom, String codeTo) {

        try {
            return new ResponseModel(factorClient.getItems());
        } catch (Exception e) {
            throw e;////////////////////
        }
    }


    public List<FactorModel> findByCreatedOnBetween(Date fromDate, Date toDate)  {

        try {
            return new ResponseModel(factorClient.getItems());
        } catch (Exception e) {
            throw e;////////////////////
        }
    }
  public List<FactorModel> findByCode(String factorCode)  {

        try {
            return new ResponseModel(factorClient.getItems());
        } catch (Exception e) {
            throw e;////////////////////
        }
    }
 public List<FactorModel> findById(String factorId)  {

        try {
            return new ResponseModel(factorClient.getItems());
        } catch (Exception e) {
            throw e;////////////////////
        }
    }








}
