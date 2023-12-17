package com.cyancoder.buyer.command;


import com.cyancoder.buyer.event.BuyerCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class BuyerAggregate {

    @AggregateIdentifier
    private Long buyerId;

    public BuyerAggregate(){

    }

    @CommandHandler
    public BuyerAggregate(CreateBuyerCommand createBuyerCommand){

        // validations



    }





    @EventSourcingHandler
    public void on(BuyerCreatedEvent buyerCreatedEvent){
        this.buyerId = buyerCreatedEvent.getFactorId();




    }


}
