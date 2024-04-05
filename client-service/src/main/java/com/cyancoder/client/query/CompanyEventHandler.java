package com.cyancoder.client.query;


import com.cyancoder.client.entity.ClientEntity;
import com.cyancoder.client.entity.CompanyEntity;
import com.cyancoder.client.event.ClientCreatedEvent;
import com.cyancoder.client.event.CompanyCreatedEvent;
import com.cyancoder.client.repository.ClientRepository;
import com.cyancoder.client.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyEventHandler {


    private  final CompanyRepository companyRepository;


    @ExceptionHandler(value=Exception.class) // need to consider: add it to main method
    public void handle(Exception exception) throws Exception {
        // log
        throw exception;
    }

    @EventHandler
    public void on(CompanyCreatedEvent event){

        CompanyEntity companyEntity = new CompanyEntity();
        BeanUtils.copyProperties(event, companyEntity);

        companyRepository.save(companyEntity); // need try catch
    }

}
