package com.cyancoder.factor.command;


import com.cyancoder.buyer.event.FactorCreatedEvent;
import com.cyancoder.buyer.model.FactorItemModel;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Aggregate
public class FactorAggregate {

    @AggregateIdentifier
    private String factorId;

    private String code;
    private String note;

    private List<FactorItemModel> items;


    public  FactorAggregate(){

    }

    @CommandHandler
    public  FactorAggregate(CreateFactorCommand createFactorCommand){

        // validations

        FactorCreatedEvent factorCreatedEvent = new FactorCreatedEvent();
        BeanUtils.copyProperties(createFactorCommand, factorCreatedEvent);

        AggregateLifecycle.apply(factorCreatedEvent);

    }

    @EventSourcingHandler
    public void on(FactorCreatedEvent factorCreatedEvent){
        this.factorId = factorCreatedEvent.getFactorId();
        this.code = factorCreatedEvent.getCode();
        this.note = factorCreatedEvent.getNote();
        this.items = factorCreatedEvent.getItems();




    }


}
