package com.cyancoder.client.command;


import com.cyancoder.client.event.ClientCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;


@Aggregate
@Slf4j
public class ClientAggregate {

    @AggregateIdentifier
    private String clientId;

    private String name;
    private Long nationalCode;
    private String economicCode;
    private String uniqueCode;
    private String pk;
    private String legalType;
    private String tell;
    private String address;
    private String postCode;
    private String cityId;

    public ClientAggregate() {

    }

    @CommandHandler
    public ClientAggregate(CreateClientCommand createClientCommand) {

        // validations

        log.info("createClientCommand: {}",createClientCommand);

        ClientCreatedEvent clientCreatedEvent = new ClientCreatedEvent();
        BeanUtils.copyProperties(createClientCommand, clientCreatedEvent);

        AggregateLifecycle.apply(clientCreatedEvent);
    }


    @EventSourcingHandler
    public void on(ClientCreatedEvent clientCreatedEvent) {

        this.clientId = clientCreatedEvent.getClientId();
        this.name = clientCreatedEvent.getName();
        this.nationalCode = clientCreatedEvent.getNationalCode();
        this.economicCode = clientCreatedEvent.getEconomicCode();
        this.uniqueCode = clientCreatedEvent.getUniqueCode();
        this.pk = clientCreatedEvent.getPk();
        this.tell = clientCreatedEvent.getTell();
        this.address = clientCreatedEvent.getAddress();
        this.postCode = clientCreatedEvent.getPostCode();
        this.cityId = clientCreatedEvent.getCityId();
    }


}
