//package com.cyancoder.buyer.saga;
//
//
//import com.cyancoder.buyer.event.BuyerCreatedEvent;
//import com.cyancoder.generic.command.buyer.AddOrEditBuyerCommand;
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
//public class BuyerSaga {
//
//
//    @Autowired
//    private transient CommandGateway commandGateway;
//
//    @StartSaga
//    @SagaEventHandler(associationProperty = "buyerId")
//    public  void handle(BuyerCreatedEvent buyerCreatedEvent){
//
//        AddOrEditBuyerCommand addOrEditBuyerCommand = AddOrEditBuyerCommand.builder()
//                .buyerId(buyerCreatedEvent.getBuyerId())
//                .build();
//
//        commandGateway.send(addOrEditBuyerCommand, new CommandCallback<AddOrEditBuyerCommand, Object>() {
//
//            @Override
//            public void onResult(@Nonnull CommandMessage<? extends AddOrEditBuyerCommand> commandMessage, @Nonnull CommandResultMessage<?> commandResultMessage) {
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
