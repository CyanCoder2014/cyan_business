package com.cyancoder.factor.command;


import com.cyancoder.factor.event.FactorCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class FactorAggregate {

    @AggregateIdentifier
    private Long factorId;

    public  FactorAggregate(){

    }

    @CommandHandler
    public  FactorAggregate(CreateFactorCommand createFactorCommand){

        // validations



    }

    @EventSourcingHandler
    public void on(FactorCreatedEvent factorCreatedEvent){
        this.factorId = factorCreatedEvent.getFactorId();




    }


}
