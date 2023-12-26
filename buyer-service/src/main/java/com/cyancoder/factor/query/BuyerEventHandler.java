package com.cyancoder.factor.query;


import com.cyancoder.factor.entity.BuyerEntity;
import com.cyancoder.factor.event.BuyerCreatedEvent;
import com.cyancoder.factor.repository.BuyerRepository;
import com.cyancoder.generic.event.BuyerAddedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuyerEventHandler {


    private  final BuyerRepository buyerRepository;


    @ExceptionHandler(value=Exception.class) // need to consider: add it to main method
    public void handle(Exception exception) throws Exception {
        // log
        throw exception;
    }

    @EventHandler
    public void on(BuyerCreatedEvent event){

        BuyerEntity buyerEntity = new BuyerEntity();
        BeanUtils.copyProperties(event, buyerEntity);

        buyerRepository.save(buyerEntity); // need try catch

    }


    @EventHandler
    public void on(BuyerAddedEvent event){

        BuyerEntity buyerEntity = new BuyerEntity();
        BeanUtils.copyProperties(event, buyerEntity);


        buyerRepository.save(buyerEntity); // need try catch

    }
}
