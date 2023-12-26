package com.cyancoder.factor.query;


import com.cyancoder.factor.entity.BuyerEntity;
import com.cyancoder.factor.model.BuyerModel;
import com.cyancoder.factor.repository.BuyerRepository;
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

        List<BuyerModel> factors = new ArrayList<>();

        List<BuyerEntity> storedFactors = buyerRepository.findAll();

//        storedFactors.forEach(item ->{
//            BuyerModel buyerModel = new BuyerModel();
//            BeanUtils.copyProperties(item, buyerModel);
//            factors.add(buyerModel);
//        });
        factors = storedFactors.stream().map(BuyerModel::new).collect(Collectors.toList());

        return factors;

    }



    @QueryHandler
    public BuyerModel fetchBuyer(FetchBuyerQuery query){


        BuyerEntity buyerDetail = buyerRepository.findByBuyerId(query.getBuyerId());

        return BuyerModel.builder()
                .id(buyerDetail.getId())
                .build();

    }



}
