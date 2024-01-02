package com.cyancoder.client.command;


import com.cyancoder.client.event.ClientCreatedEvent;
import com.cyancoder.generic.command.client.AddOrEditClientCommand;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
@Slf4j
public class ClientAggregate {

    @AggregateIdentifier
    private String clientId;

    public ClientAggregate(){

    }

    @CommandHandler
    public ClientAggregate(CreateClientCommand createClientCommand){

        // validations


        ClientCreatedEvent clientCreatedEvent = ClientCreatedEvent.builder()
                .clientId(createClientCommand.getClientId())
                .build();

        AggregateLifecycle.apply(clientCreatedEvent);

    }

    @CommandHandler
    public ClientAggregate(AddOrEditClientCommand addOrEditClientCommand){

        // validations


        ClientCreatedEvent clientCreatedEvent = ClientCreatedEvent.builder()
                .clientId(addOrEditClientCommand.getClientId())
                .build();

        AggregateLifecycle.apply(clientCreatedEvent);

    }


//    @CommandHandler
//    public void handle(AddOrEditClientCommand addOrEditClientCommand) throws Exception {
//
//        if (addOrEditClientCommand.getClientId() == null)
//            throw new Exception("error!");
//
//        ClientCreatedEvent clientCreatedEvent = ClientCreatedEvent.builder()
//                .clientId(addOrEditClientCommand.getClientId())
//                .build();
//
//        AggregateLifecycle.apply(clientCreatedEvent);
//
//    }





    @EventSourcingHandler
    public void on(ClientCreatedEvent clientCreatedEvent){
        this.clientId = clientCreatedEvent.getClientId();
    }
//
//    @EventSourcingHandler
//    public void on(ClientCreatedEvent clientCreatedEvent){
//        this.clientId = clientCreatedEvent.getClientId();//////////
//    }

}
