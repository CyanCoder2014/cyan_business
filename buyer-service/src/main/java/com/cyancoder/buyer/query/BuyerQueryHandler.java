package com.cyancoder.buyer.query;


import com.cyancoder.buyer.entity.BuyerEntity;
import com.cyancoder.buyer.model.BuyerModel;
import com.cyancoder.buyer.repository.BuyerRepository;
import com.cyancoder.generic.query.FetchBuyerQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuyerQueryHandler {

    private  final BuyerRepository buyerRepository;

    @QueryHandler
    public List<BuyerModel> filterBuyer(FilterBuyerQuery query){

        List<BuyerModel> buyers = new ArrayList<>();

        List<BuyerEntity> storedBuyers = buyerRepository.findAll();

//        storedBuyers.forEach(item ->{
//            BuyerModel buyerModel = new BuyerModel();
//            BeanUtils.copyProperties(item, buyerModel);
//            buyers.add(buyerModel);
//        });
        buyers = storedBuyers.stream().map(BuyerModel::new).collect(Collectors.toList());

        return buyers;

    }



    @QueryHandler
    public BuyerModel fetchBuyer(FetchBuyerQuery query){


        BuyerEntity buyerDetail = buyerRepository.findByBuyerId(query.getBuyerId());

        return BuyerModel.builder()
                .buyerId(buyerDetail.getBuyerId())
                .build();

    }



}
