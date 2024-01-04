package com.cyancoder.client.command;


import com.cyancoder.client.event.BuyerCreatedEvent;
import com.cyancoder.generic.command.buyer.AddOrEditBuyerCommand;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
@Slf4j
public class BuyerAggregate {

    @AggregateIdentifier
    private String buyerId;

    private Long nationalCode;
    private String economicCode;
    private String buyerType;
    private String tell;
    private String address;
    private String postCode;
    private String cityId;
    private String note;
    private boolean addNew;

    public BuyerAggregate(){

    }

    @CommandHandler
    public BuyerAggregate(CreateBuyerCommand createBuyerCommand){

        // validations


        BuyerCreatedEvent buyerCreatedEvent = BuyerCreatedEvent.builder()
                .buyerId(createBuyerCommand.getBuyerId())
                .build();

        AggregateLifecycle.apply(buyerCreatedEvent);

    }

    @CommandHandler
    public BuyerAggregate(AddOrEditBuyerCommand addOrEditBuyerCommand){

        // validations


        BuyerCreatedEvent buyerCreatedEvent = BuyerCreatedEvent.builder()
                .buyerId(addOrEditBuyerCommand.getBuyerId())
                .nationalCode(addOrEditBuyerCommand.getNationalCode())
                .economicCode(addOrEditBuyerCommand.getEconomicCode())
                .buyerType(addOrEditBuyerCommand.getBuyerType())
                .tell(addOrEditBuyerCommand.getTell())
                .address(addOrEditBuyerCommand.getAddress())
                .postCode(addOrEditBuyerCommand.getPostCode())
                .cityId(addOrEditBuyerCommand.getCityId())
                .addNew(addOrEditBuyerCommand.isAddNew())
                .build();

        AggregateLifecycle.apply(buyerCreatedEvent);

    }


//    @CommandHandler
//    public void handle(AddOrEditBuyerCommand addOrEditBuyerCommand) throws Exception {
//
//        if (addOrEditBuyerCommand.getBuyerId() == null)
//            throw new Exception("error!");
//
//        BuyerCreatedEvent buyerCreatedEvent = BuyerCreatedEvent.builder()
//                .buyerId(addOrEditBuyerCommand.getBuyerId())
//                .build();
//
//        AggregateLifecycle.apply(buyerCreatedEvent);
//
//    }





    @EventSourcingHandler
    public void on(BuyerCreatedEvent buyerCreatedEvent){
        this.buyerId = buyerCreatedEvent.getBuyerId();
    }
//
//    @EventSourcingHandler
//    public void on(BuyerCreatedEvent buyerCreatedEvent){
//        this.buyerId = buyerCreatedEvent.getBuyerId();//////////
//    }

}
