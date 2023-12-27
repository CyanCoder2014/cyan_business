package com.cyancoder.buyer.saga;


import com.cyancoder.buyer.event.FactorCreatedEvent;
import com.cyancoder.buyer.model.BuyerModel;
import com.cyancoder.generic.command.buyer.AddOrEditBuyerCommand;
import com.cyancoder.generic.event.BuyerAddedEvent;
import com.cyancoder.generic.query.FetchBuyerQuery;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

@Saga
@Slf4j
public class FactorSaga {


    @Autowired
    private transient CommandGateway commandGateway;
    @Autowired
    private transient QueryGateway queryGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "factorId")
    public void handle(FactorCreatedEvent factorCreatedEvent) {

        log.info("saga started ");


        AddOrEditBuyerCommand addOrEditBuyerCommand = AddOrEditBuyerCommand.builder()
                .buyerId(factorCreatedEvent.getBuyerId())
                .build();

        commandGateway.send(addOrEditBuyerCommand, new CommandCallback<AddOrEditBuyerCommand, Object>() {

            @Override
            public void onResult(@Nonnull CommandMessage<? extends AddOrEditBuyerCommand> commandMessage, @Nonnull CommandResultMessage<?> commandResultMessage) {

                if (commandResultMessage.isExceptional()) {
                    // start compensating transaction
                }
            }
        });

    }



    @SagaEventHandler(associationProperty = "buyerId")
    public void handle(BuyerAddedEvent buyerAddedEvent) {

        log.info("Buyer added: " + buyerAddedEvent.getBuyerId());


        FetchBuyerQuery fetchBuyerQuery = new FetchBuyerQuery(buyerAddedEvent.getBuyerId());

        BuyerModel buyer = null;

        try {
             buyer = queryGateway.query(fetchBuyerQuery,
                    ResponseTypes.instanceOf(BuyerModel.class)).join();

        }catch (Exception e){

            return;

        }


        if (buyer == null){

            //Start compensation transaction
            return;
        }

    }

}
