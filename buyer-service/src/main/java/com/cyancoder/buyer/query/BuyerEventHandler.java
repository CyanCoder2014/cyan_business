package com.cyancoder.buyer.query;


import com.cyancoder.buyer.entity.BuyerEntity;
import com.cyancoder.buyer.event.BuyerCreatedEvent;
import com.cyancoder.buyer.repository.BuyerRepository;
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
}
