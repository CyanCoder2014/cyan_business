package com.cyancoder.factor.command;


import com.cyancoder.factor.event.BuyerCreatedEvent;
import com.cyancoder.generic.command.buyer.AddOrEditBuyerCommand;
import com.cyancoder.generic.event.BuyerAddedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
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


    @CommandHandler
    public void handle(AddOrEditBuyerCommand addOrEditBuyerCommand) throws Exception {



        if (addOrEditBuyerCommand.getBuyerId() == null)
            throw new Exception("error!");


        BuyerAddedEvent buyerCreatedEvent = BuyerAddedEvent.builder()
                .buyerId(addOrEditBuyerCommand.getBuyerId())
                .build();

        AggregateLifecycle.apply(buyerCreatedEvent);







    }





    @EventSourcingHandler
    public void on(BuyerCreatedEvent buyerCreatedEvent){
        this.buyerId = buyerCreatedEvent.getBuyerId();



    }

    @EventSourcingHandler
    public void on(BuyerAddedEvent buyerAddedEvent){
        this.buyerId = buyerAddedEvent.getBuyerId();//////////



    }

}
