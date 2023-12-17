package com.cyancoder.buyer.query;


import com.cyancoder.buyer.entity.BuyerEntity;
import com.cyancoder.buyer.model.BuyerModel;
import com.cyancoder.buyer.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuyerQueryHandler {

    private  final BuyerRepository buyerRepository;

    @QueryHandler
    public List<BuyerModel> filterFactor(FilterBuyerQuery query){

        List<BuyerModel> factors = new ArrayList<>();

        List<BuyerEntity> storedFactors = buyerRepository.findAll();

        storedFactors.forEach(item ->{
            BuyerModel buyerModel = new BuyerModel();
            BeanUtils.copyProperties(item, buyerModel);
            factors.add(buyerModel);
        });

        return factors;

    }




}
