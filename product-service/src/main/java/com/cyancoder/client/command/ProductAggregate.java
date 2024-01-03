package com.cyancoder.client.command;


import com.cyancoder.client.event.ProductCreatedEvent;
//import com.cyancoder.generic.command.product.AddOrEditProductCommand;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
@Slf4j
public class ProductAggregate {

    @AggregateIdentifier
    private String productId;

    public ProductAggregate(){

    }

    @CommandHandler
    public ProductAggregate(CreateProductCommand createProductCommand){

        // validations


        ProductCreatedEvent productCreatedEvent = ProductCreatedEvent.builder()
                .productId(createProductCommand.getProductId())
                .build();

        AggregateLifecycle.apply(productCreatedEvent);

    }

//    @CommandHandler
//    public ProductAggregate(AddOrEditProductCommand addOrEditProductCommand){
//
//        // validations
//
//
//        ProductCreatedEvent productCreatedEvent = ProductCreatedEvent.builder()
//                .productId(addOrEditProductCommand.getProductId())
//                .build();
//
//        AggregateLifecycle.apply(productCreatedEvent);
//
//    }


//    @CommandHandler
//    public void handle(AddOrEditProductCommand addOrEditProductCommand) throws Exception {
//
//        if (addOrEditProductCommand.getProductId() == null)
//            throw new Exception("error!");
//
//        ProductCreatedEvent productCreatedEvent = ProductCreatedEvent.builder()
//                .productId(addOrEditProductCommand.getProductId())
//                .build();
//
//        AggregateLifecycle.apply(productCreatedEvent);
//
//    }





    @EventSourcingHandler
    public void on(ProductCreatedEvent productCreatedEvent){
        this.productId = productCreatedEvent.getProductId();
    }
//
//    @EventSourcingHandler
//    public void on(ProductCreatedEvent productCreatedEvent){
//        this.productId = productCreatedEvent.getProductId();//////////
//    }

}
