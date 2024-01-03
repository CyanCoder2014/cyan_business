//package com.cyancoder.product.saga;
//
//
//import com.cyancoder.product.event.ProductCreatedEvent;
//import com.cyancoder.generic.command.product.AddOrEditProductCommand;
//import org.axonframework.commandhandling.CommandCallback;
//import org.axonframework.commandhandling.CommandMessage;
//import org.axonframework.commandhandling.CommandResultMessage;
//import org.axonframework.commandhandling.gateway.CommandGateway;
//import org.axonframework.modelling.saga.SagaEventHandler;
//import org.axonframework.modelling.saga.StartSaga;
//import org.axonframework.spring.stereotype.Saga;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import javax.annotation.Nonnull;
//
//@Saga
//public class ProductSaga {
//
//
//    @Autowired
//    private transient CommandGateway commandGateway;
//
//    @StartSaga
//    @SagaEventHandler(associationProperty = "productId")
//    public  void handle(ProductCreatedEvent productCreatedEvent){
//
//        AddOrEditProductCommand addOrEditProductCommand = AddOrEditProductCommand.builder()
//                .productId(productCreatedEvent.getProductId())
//                .build();
//
//        commandGateway.send(addOrEditProductCommand, new CommandCallback<AddOrEditProductCommand, Object>() {
//
//            @Override
//            public void onResult(@Nonnull CommandMessage<? extends AddOrEditProductCommand> commandMessage, @Nonnull CommandResultMessage<?> commandResultMessage) {
//
//                if (commandResultMessage.isExceptional()){
//                    // start compensating transaction
//                }
//            }
//        });
//
//
//
//
//    }
//
//}
