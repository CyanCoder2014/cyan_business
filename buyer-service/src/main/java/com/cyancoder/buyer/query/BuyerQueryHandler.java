package com.cyancoder.buyer.query;


import com.cyancoder.buyer.entity.BuyerEntity;
import com.cyancoder.buyer.repository.BuyerRepository;
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

//        storedBuyers.forEach(item ->{
//            BuyerModel buyerModel = new BuyerModel();
//            BeanUtils.copyProperties(item, buyerModel);
//            buyers.add(buyerModel);
//        });
//        buyers = storedBuyers.stream().map(BuyerModel::new).collect(Collectors.toList());//////////////

        return buyers;

    }



    @QueryHandler
    public Buyer fetchBuyer(FetchBuyerQuery query){

        log.info("query::::: {}",query);


        BuyerEntity buyerEntity = buyerRepository.findByBuyerId(query.getBuyerId());
        log.info("buyerEntity::::: {}",query);

        return Buyer.builder()
                .buyerId(buyerEntity.getBuyerId())
                .build();

    }



}
