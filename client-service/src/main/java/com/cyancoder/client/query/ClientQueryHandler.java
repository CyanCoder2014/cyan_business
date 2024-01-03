package com.cyancoder.client.query;


import com.cyancoder.client.entity.ClientEntity;
import com.cyancoder.client.model.ClientModel;
import com.cyancoder.client.repository.ClientRepository;
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
    public List<ClientModel> filterClient(FilterClientQuery query){

        List<ClientModel> clients = new ArrayList<>();
        List<ClientEntity> storedClients = clientRepository.findAll();

        storedClients.forEach(item->{
            ClientModel model = new ClientModel(item);
            clients.add(model);
        });

        return clients;

    }






}
