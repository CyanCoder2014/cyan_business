//package com.cyancoder.client.saga;
//
//
//import com.cyancoder.client.event.ClientCreatedEvent;
//import com.cyancoder.generic.command.client.AddOrEditClientCommand;
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
//public class ClientSaga {
//
//
//    @Autowired
//    private transient CommandGateway commandGateway;
//
//    @StartSaga
//    @SagaEventHandler(associationProperty = "clientId")
//    public  void handle(ClientCreatedEvent clientCreatedEvent){
//
//        AddOrEditClientCommand addOrEditClientCommand = AddOrEditClientCommand.builder()
//                .clientId(clientCreatedEvent.getClientId())
//                .build();
//
//        commandGateway.send(addOrEditClientCommand, new CommandCallback<AddOrEditClientCommand, Object>() {
//
//            @Override
//            public void onResult(@Nonnull CommandMessage<? extends AddOrEditClientCommand> commandMessage, @Nonnull CommandResultMessage<?> commandResultMessage) {
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
