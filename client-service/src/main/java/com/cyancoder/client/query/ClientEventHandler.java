package com.cyancoder.client.query;


import com.cyancoder.client.entity.ClientEntity;
import com.cyancoder.client.event.ClientCreatedEvent;
import com.cyancoder.client.repository.ClientRepository;
//import com.cyancoder.generic.event.ClientAddedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientEventHandler {


    private  final ClientRepository clientRepository;


    @ExceptionHandler(value=Exception.class) // need to consider: add it to main method
    public void handle(Exception exception) throws Exception {
        // log
        throw exception;
    }

    @EventHandler
    public void on(ClientCreatedEvent event){

        ClientEntity clientEntity = new ClientEntity();
        BeanUtils.copyProperties(event, clientEntity);

        clientRepository.save(clientEntity); // need try catch

    }


//    @EventHandler
//    public void on(ClientAddedEvent event){
//
//        ClientEntity clientEntity = new ClientEntity();
//        BeanUtils.copyProperties(event, clientEntity);
//
//
//        clientRepository.save(clientEntity); // need try catch
//
//    }
}
