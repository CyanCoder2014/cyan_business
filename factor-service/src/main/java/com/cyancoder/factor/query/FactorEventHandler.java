package com.cyancoder.factor.query;


import com.cyancoder.factor.entity.FactorEntity;
import com.cyancoder.factor.entity.FactorItemEntity;
import com.cyancoder.factor.event.FactorCreatedEvent;
import com.cyancoder.factor.event.FactorFilteredEvent;
import com.cyancoder.factor.model.FactorItemModel;
import com.cyancoder.factor.model.FactorModel;
import com.cyancoder.factor.repository.FactorItemRepository;
import com.cyancoder.factor.repository.FactorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@ProcessingGroup("factor-group")
public class FactorEventHandler {


    private final FactorRepository factorRepository;
    private final FactorItemRepository factorItemRepository;


    @ExceptionHandler(value = Exception.class) // need to consider: add it to main method
    public void handle(Exception exception) throws Exception {
        // log
//        throw exception;
        log.info("ExceptionHandler");
    }

    @EventHandler
    public void on(FactorCreatedEvent event) {
        log.info("FactorCreatedEvent called event handler");

        FactorEntity factorEntity = new FactorEntity();
        BeanUtils.copyProperties(event, factorEntity);
        if (factorEntity.getCode() == null) {
            long lastCode;
            try {
                lastCode = Long.parseLong(factorRepository.findTopByOrderByCodeDesc().getCode());
            } catch (Exception e) {
                lastCode = 1000000000;
            }

            factorEntity.setCode(String.valueOf(lastCode + 1));
        }
        log.info("event: {}",event);


        try{
            FactorEntity factor = factorRepository.save(factorEntity);
            log.info("factorRepository.save: {}",factor);

            List<FactorItemModel> items = event.getItems();
            items.forEach(item->{
                item.setFactorItemId(UUID.randomUUID().toString());
                item.setFactor(new FactorModel(factor));

            });
            List<FactorItemEntity> factorItems = items.stream().map(FactorItemEntity::new).collect(Collectors.toList());

            factorItemRepository.saveAll(factorItems);

        }catch (Exception ignored){

        }

    }



    @EventHandler
    public void on(FactorFilteredEvent event) {

        log.info("@EventHandler called FactorFilteredEvent !!!!!!!!!!!!: {}",event);



    }
}
