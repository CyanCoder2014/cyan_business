package com.cyancoder.client.query;


import com.cyancoder.client.entity.BuyerEntity;
import com.cyancoder.client.event.BuyerCreatedEvent;
import com.cyancoder.client.repository.BuyerRepository;
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


    private final BuyerRepository buyerRepository;


    @ExceptionHandler(value = Exception.class) // need to consider: add it to main method
    public void handle(Exception exception) throws Exception {
        // log
        throw exception;
    }

    @EventHandler
    public void on(BuyerCreatedEvent event) {

        BuyerEntity buyerEntity = new BuyerEntity();
//        BeanUtils.copyProperties(event, buyerEntity);

        buyerEntity.setBuyerId(event.getBuyerId());
        buyerEntity.setNationalCode(event.getNationalCode());
        buyerEntity.setEconomicCode(event.getEconomicCode());
        buyerEntity.setBuyerType(event.getBuyerType());
        buyerEntity.setTell(event.getTell());
        buyerEntity.setAddress(event.getAddress());
        buyerEntity.setPostCode(event.getPostCode());
        buyerEntity.setCityId(event.getCityId());

//        BuyerEntity buyerEntityOld = buyerRepository.findFirstByNationalCode(event.getNationalCode());
//        BuyerEntity buyerEntityOld = buyerRepository.findByBuyerId(event.getBuyerId());
//        if (!event.isAddNew() && buyerEntityOld != null){
//            buyerEntity.setBuyerId(buyerEntityOld.getBuyerId());
//        }


        buyerRepository.save(buyerEntity); // need try catch


    }


    @EventHandler
    public void on(BuyerAddedEvent event) {

        BuyerEntity buyerEntity = new BuyerEntity();
        BeanUtils.copyProperties(event, buyerEntity);


        buyerRepository.save(buyerEntity); // need try catch

    }
}
