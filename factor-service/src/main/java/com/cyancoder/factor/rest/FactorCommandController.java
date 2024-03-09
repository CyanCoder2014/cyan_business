package com.cyancoder.factor.rest;


import com.cyancoder.factor.command.CreateFactorCommand;
import com.cyancoder.factor.entity.FactorEntity;
import com.cyancoder.factor.entity.FactorItemEntity;
import com.cyancoder.factor.entity.ProductEntity;
import com.cyancoder.factor.entity.UnitEntity;
import com.cyancoder.factor.model.FactorItemModel;
import com.cyancoder.factor.model.FactorModel;
import com.cyancoder.factor.model.ProductModel;
import com.cyancoder.factor.model.request.CreateFactorReqModel;
import com.cyancoder.factor.repository.FactorItemRepository;
import com.cyancoder.factor.repository.FactorRepository;
import com.cyancoder.factor.repository.ProductRepository;
import com.cyancoder.factor.repository.UnitRepository;
import com.cyancoder.factor.service.FactorService;
import com.cyancoder.generic.command.buyer.AddOrEditBuyerCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v2/api/factor-service/factors")
@RequiredArgsConstructor
@Slf4j
public class FactorCommandController {


    private  final Environment env;
    private  final CommandGateway commandGateway;



    private final FactorService factorService;


    @PostMapping
    public Object createFactor(@RequestBody CreateFactorReqModel createFactorReqModel) throws ParseException {

        return factorService.addFactor(createFactorReqModel);

    }

    @DeleteMapping
    public Object createFactor(@RequestParam String factorId) {

        return factorService.removeFactor(factorId);

    }


}
