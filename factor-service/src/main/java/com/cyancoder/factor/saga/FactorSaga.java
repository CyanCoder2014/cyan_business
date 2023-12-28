package com.cyancoder.factor.saga;


import com.cyancoder.factor.event.FactorCreatedEvent;
import com.cyancoder.factor.event.FactorFilteredEvent;
import com.cyancoder.factor.query.GetBuyerQuery;
import com.cyancoder.generic.command.buyer.AddOrEditBuyerCommand;
import com.cyancoder.generic.model.Buyer;
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
import java.util.concurrent.TimeUnit;

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
//                .buyerId(factorCreatedEvent.getBuyerId())
                .buyerId(factorCreatedEvent.getBuyerId())
                .build();

        log.info("addOrEditBuyerCommand: {} ",addOrEditBuyerCommand);


        commandGateway.send(addOrEditBuyerCommand, new CommandCallback<AddOrEditBuyerCommand, Object>() {

            @Override
            public void onResult(@Nonnull CommandMessage<? extends AddOrEditBuyerCommand> commandMessage, @Nonnull CommandResultMessage<?> commandResultMessage) {

                if (commandResultMessage.isExceptional()) {
                    // start compensating transaction
                }
            }
        });

    }


//    @StartSaga
//    @SagaEventHandler(associationProperty = "buyerId")
//    public void handle(FactorFilteredEvent factorFilteredEvent) {
//
//        log.info("Buyer get: " + factorFilteredEvent.getBuyerId());
//
//
//        FetchBuyerQuery fetchBuyerQuery = new FetchBuyerQuery(factorFilteredEvent.getBuyerId());
//
//        Buyer buyer = null;
//
//        try {
//             buyer = queryGateway.query(fetchBuyerQuery,
//                    ResponseTypes.instanceOf(Buyer.class)).join();
//
//        }catch (Exception e){
//
//
//        }
//
//
//        if (buyer == null){
//
//            //Start compensation transaction
//            return;
//        }
//
//
//
//        GetBuyerQuery getBuyerQuery = GetBuyerQuery.builder()
//                .buyerId(buyer.getBuyerId())
//                .build();
//        try{
//
//            commandGateway.sendAndWait(getBuyerQuery,10, TimeUnit.SECONDS);
//
//        }catch (Exception e){
//            // start compensating transaction
//        }
//
//
//
//    }

}
