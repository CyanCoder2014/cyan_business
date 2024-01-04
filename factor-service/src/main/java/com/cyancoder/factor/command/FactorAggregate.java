package com.cyancoder.factor.command;


import com.cyancoder.factor.event.FactorCreatedEvent;
import com.cyancoder.factor.event.FactorFilteredEvent;
import com.cyancoder.factor.model.BuyerModel;
import com.cyancoder.factor.model.FactorItemModel;
import com.cyancoder.factor.query.FilterFactorQuery;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

@Aggregate
@Slf4j
public class FactorAggregate {

    @AggregateIdentifier
    private String factorId;

    private String code;

    private List<FactorItemModel> items;

    private BuyerModel buyer;

    private Date factorDate;
    private String payType;
    private Double payed;
    private String state;
    private String note;


    public  FactorAggregate(){

    }

    @CommandHandler
    public  FactorAggregate(CreateFactorCommand createFactorCommand){


        log.info("createFactorCommand::: {}", createFactorCommand);
        // validations

        FactorCreatedEvent factorCreatedEvent = new FactorCreatedEvent();
        BeanUtils.copyProperties(createFactorCommand, factorCreatedEvent);

        AggregateLifecycle.apply(factorCreatedEvent);

    }

    @EventSourcingHandler
    public void on(FactorCreatedEvent factorCreatedEvent){
        this.factorId = factorCreatedEvent.getFactorId();
        this.code = factorCreatedEvent.getCode();
        this.factorDate = factorCreatedEvent.getFactorDate();
        this.payType = factorCreatedEvent.getPayType();
        this.payed = factorCreatedEvent.getPayed();
        this.state = factorCreatedEvent.getState();
        this.note = factorCreatedEvent.getNote();
        this.items = factorCreatedEvent.getItems();
        this.buyer = factorCreatedEvent.getBuyer();
    }





    @CommandHandler
    public  FactorAggregate(SetBuyerRelationCommand setBuyerRelationCommand){


        log.info("setBuyerRelationCommand::: {}", setBuyerRelationCommand);
        // validations

        FactorFilteredEvent factorFilteredEvent = new FactorFilteredEvent();
        BeanUtils.copyProperties(setBuyerRelationCommand, factorFilteredEvent);

        AggregateLifecycle.apply(factorFilteredEvent);

    }


    @EventSourcingHandler
    public void on(FactorFilteredEvent factorFilteredEvent){
        this.factorId = factorFilteredEvent.getFactorId();
        this.code = factorFilteredEvent.getCode();
        this.factorDate = factorFilteredEvent.getFactorDate();
        this.payType = factorFilteredEvent.getPayType();
        this.payed = factorFilteredEvent.getPayed();
        this.state = factorFilteredEvent.getState();
        this.note = factorFilteredEvent.getNote();
        this.items = factorFilteredEvent.getItems();
        this.buyer = factorFilteredEvent.getBuyer();
    }


}
