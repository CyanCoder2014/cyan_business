package com.cyancoder.product.query;


import com.cyancoder.product.entity.ClientEntity;
import com.cyancoder.product.repository.ClientRepository;
import com.cyancoder.generic.model.Client;
import com.cyancoder.generic.query.FetchClientQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientQueryHandler {

    private  final ClientRepository clientRepository;

    @QueryHandler
    public List<Client> filterClient(FilterClientQuery query){

        List<Client> clients = new ArrayList<>();

        List<ClientEntity> storedClients = clientRepository.findAll();

        return clients;

    }



    @QueryHandler
    public Client fetchClient(FetchClientQuery query){

        log.info("query::::: {}",query);


        ClientEntity clientEntity = clientRepository.findByClientId(query.getClientId());
        log.info("clientEntity::::: {}",query);

        return Client.builder()
                .clientId(clientEntity.getClientId())
                .build();

    }



}
