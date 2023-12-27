package com.cyancoder.factor.command;


import com.cyancoder.factor.event.FactorCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@Aggregate
public class FactorAggregate {

    @AggregateIdentifier
    private String factorId;

    private String code;
    private String note;

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




    }


}
