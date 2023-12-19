package com.cyancoder.buyer.saga;


import com.cyancoder.buyer.event.FactorCreatedEvent;
import com.cyancoder.generic.command.buyer.AddOrEditBuyerCommand;
import com.cyancoder.generic.event.BuyerAddedEvent;
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


    @SagaEventHandler(associationProperty = "factorId")
    public void handle(BuyerAddedEvent buyerAddedEvent) {

        log.info("Buyer added: " + buyerAddedEvent.getBuyerId());


        fetchSellerDetaQuery fetchSellerDetaQuery = new fetchSellerDetaQuery(factorEwvwnt.getSellerID());

        Seller sellerDetails = null;

        try {
            sellerDetails = queryGateway.query(fetchSellerDetaQuery,
                    ResponseTypes.instanceOf(Seller.class).joun());

        }catch (Exception e){


            return;

        }


        if (sellerDetails = null){
            //Satary compansation transaction

            return;
        }

    }

}
