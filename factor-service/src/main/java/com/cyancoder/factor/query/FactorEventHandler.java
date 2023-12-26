package com.cyancoder.factor.query;


import com.cyancoder.factor.entity.FactorEntity;
import com.cyancoder.factor.event.FactorCreatedEvent;
import com.cyancoder.factor.repository.FactorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactorEventHandler {



    private final FactorRepository factorRepository;


    @ExceptionHandler(value=Exception.class) // need to consider: add it to main method
    public void handle(Exception exception) throws Exception {
        // log
        throw exception;
    }

    @EventHandler
    public void on(FactorCreatedEvent event){

        FactorEntity factorEntity = new FactorEntity();
        BeanUtils.copyProperties(event,factorEntity);

        factorRepository.save(factorEntity); // need try catch

    }
}
