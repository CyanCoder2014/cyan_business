package com.cyancoder.client.query;


import com.cyancoder.client.entity.ProductEntity;
import com.cyancoder.client.event.ProductCreatedEvent;
import com.cyancoder.client.repository.ProductRepository;
//import com.cyancoder.generic.event.ProductAddedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventHandler {


    private  final ProductRepository productRepository;


    @ExceptionHandler(value=Exception.class) // need to consider: add it to main method
    public void handle(Exception exception) throws Exception {
        // log
        throw exception;
    }

    @EventHandler
    public void on(ProductCreatedEvent event){

        ProductEntity productEntity = new ProductEntity();
        BeanUtils.copyProperties(event, productEntity);

        productRepository.save(productEntity); // need try catch

    }


//    @EventHandler
//    public void on(ProductAddedEvent event){
//
//        ProductEntity productEntity = new ProductEntity();
//        BeanUtils.copyProperties(event, productEntity);
//
//
//        productRepository.save(productEntity); // need try catch
//
//    }
}
