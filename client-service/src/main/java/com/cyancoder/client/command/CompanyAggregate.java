package com.cyancoder.client.command;


import com.cyancoder.client.event.CompanyCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;


@Aggregate
@Slf4j
public class CompanyAggregate {

    @AggregateIdentifier
    private String companyId;

    private String clientId;
    private String name;
    private Long nationalCode;
    private String economicCode;
    private String uniqueCode;
    private String pk;
    private String legalType;
    private String tell;
    private String address;
    private String postCode;
    private String cityId;

    public CompanyAggregate() {

    }

    @CommandHandler
    public CompanyAggregate(CreateCompanyCommand createCompanyCommand) {

        // validations
        log.info("createCompanyCommand: {}",createCompanyCommand);


        CompanyCreatedEvent companyCreatedEvent = new CompanyCreatedEvent();
        BeanUtils.copyProperties(createCompanyCommand, companyCreatedEvent);

        AggregateLifecycle.apply(companyCreatedEvent);
    }


    @EventSourcingHandler
    public void on(CompanyCreatedEvent companyCreatedEvent) {
        log.info("companyCreatedEvent: {}",companyCreatedEvent);

        this.companyId = companyCreatedEvent.getCompanyId();
        this.clientId = companyCreatedEvent.getClientId();
        this.name = companyCreatedEvent.getName();
        this.nationalCode = companyCreatedEvent.getNationalCode();
        this.economicCode = companyCreatedEvent.getEconomicCode();
        this.uniqueCode = companyCreatedEvent.getUniqueCode();
        this.pk = companyCreatedEvent.getPk();
        this.tell = companyCreatedEvent.getTell();
        this.address = companyCreatedEvent.getAddress();
        this.postCode = companyCreatedEvent.getPostCode();
        this.cityId = companyCreatedEvent.getCityId();
    }


}
