package com.cyancoder.buyer.query;


import com.cyancoder.buyer.entity.FactorEntity;
import com.cyancoder.buyer.model.FactorModel;
import com.cyancoder.buyer.repository.FactorRepository;
import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FactorQueryHandler {

    private  final FactorRepository factorRepository;

    @QueryHandler
    public List<FactorModel> filterFactor(FilterFactorQuery query){

        List<FactorModel> factors = new ArrayList<>();

        List<FactorEntity> storedFactors = factorRepository.findAll();

        storedFactors.forEach(item ->{
            FactorModel factorModel = new FactorModel();
            BeanUtils.copyProperties(item, factorModel);
            factors.add(factorModel);
        });

        return factors;

    }




}
