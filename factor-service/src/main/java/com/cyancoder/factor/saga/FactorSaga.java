package com.cyancoder.factor.saga;


import com.cyancoder.factor.event.FactorCreatedEvent;
import com.cyancoder.generic.command.buyer.AddOrEditBuyerCommand;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

@Saga
@Slf4j
public class FactorSaga {


    @Autowired
    private transient CommandGateway commandGateway;


    @StartSaga
    @SagaEventHandler(associationProperty = "factorId")
    public void handle(FactorCreatedEvent factorCreatedEvent) {

        log.info("saga started ");


        AddOrEditBuyerCommand addOrEditBuyerCommand = AddOrEditBuyerCommand.builder()
                .buyerId(factorCreatedEvent.getBuyerId())
                .build();

        log.info("addOrEditBuyerCommand: {} ",addOrEditBuyerCommand);


        commandGateway.send(addOrEditBuyerCommand, (commandMessage, commandResultMessage) -> {

            if (commandResultMessage.isExceptional()) {
                // start compensating transaction
            }
        });

    }


}
