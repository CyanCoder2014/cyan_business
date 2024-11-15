package com.cyancoder.client.query;


import com.cyancoder.client.entity.BuyerEntity;
import com.cyancoder.client.repository.BuyerRepository;
import com.cyancoder.generic.model.Buyer;
import com.cyancoder.generic.query.FetchBuyerQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuyerQueryHandler {

    private  final BuyerRepository buyerRepository;

    @QueryHandler
    public List<Buyer> filterBuyer(FilterBuyerQuery query){

        List<Buyer> buyers = new ArrayList<>();

        List<BuyerEntity> storedBuyers = buyerRepository.findAll();

        return buyers;

    }



    @QueryHandler
    public Buyer fetchBuyer(FetchBuyerQuery query){


        BuyerEntity buyerEntity = buyerRepository.findByBuyerId(query.getBuyerId());

        return Buyer.builder()
                .buyerId(buyerEntity.getBuyerId())
                .nationalCode(buyerEntity.getNationalCode())
                .economicCode(buyerEntity.getEconomicCode())
                .buyerType(buyerEntity.getBuyerType())
                .tell(buyerEntity.getTell())
                .address(buyerEntity.getAddress())
                .postCode(buyerEntity.getPostCode())
                .cityId(buyerEntity.getCityId())
                .build();

    }



}
