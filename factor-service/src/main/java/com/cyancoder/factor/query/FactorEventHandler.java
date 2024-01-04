package com.cyancoder.factor.query;


import com.cyancoder.factor.entity.FactorEntity;
import com.cyancoder.factor.entity.FactorItemEntity;
import com.cyancoder.factor.entity.ProductEntity;
import com.cyancoder.factor.entity.UnitEntity;
import com.cyancoder.factor.event.FactorCreatedEvent;
import com.cyancoder.factor.event.FactorFilteredEvent;
import com.cyancoder.factor.model.FactorItemModel;
import com.cyancoder.factor.model.FactorModel;
import com.cyancoder.factor.model.ProductModel;
import com.cyancoder.factor.repository.FactorItemRepository;
import com.cyancoder.factor.repository.FactorRepository;
import com.cyancoder.factor.repository.ProductRepository;
import com.cyancoder.factor.repository.UnitRepository;
import com.cyancoder.generic.command.buyer.AddOrEditBuyerCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@ProcessingGroup("factor-group")
public class FactorEventHandler {


    private final FactorRepository factorRepository;
    private final FactorItemRepository factorItemRepository;
    private final ProductRepository productRepository;
    private final UnitRepository unitRepository;
    private final CommandGateway commandGateway;

    @ExceptionHandler(value = Exception.class) // need to consider: add it to main method
    public void handle(Exception exception) throws Exception {
        // log
//        throw exception;
        log.info("ExceptionHandler");
    }

    @EventHandler
    public void on(FactorCreatedEvent event) {
        log.info("FactorCreatedEvent called event handler");

        FactorEntity factorEntity = new FactorEntity();
        BeanUtils.copyProperties(event, factorEntity);
        if (factorEntity.getCode() == null) {
            long lastCode;
            try {
                lastCode = Long.parseLong(factorRepository.findTopByOrderByCodeDesc().getCode());
            } catch (Exception e) {
                lastCode = 1000000000;
            }

            factorEntity.setCode(String.valueOf(lastCode + 1));
        }
        log.info("event: {}", event);


        AddOrEditBuyerCommand addOrEditBuyerCommand = AddOrEditBuyerCommand.builder()
                .buyerId(event.getBuyer().getBuyerId() != null ? event.getBuyer().getBuyerId() : UUID.randomUUID().toString())
                .nationalCode(event.getBuyer().getNationalCode())
                .economicCode(event.getBuyer().getEconomicCode())
                .buyerType(event.getBuyer().getBuyerType())
                .tell(event.getBuyer().getTell())
                .address(event.getBuyer().getAddress())
                .postCode(event.getBuyer().getPostCode())
                .cityId(event.getBuyer().getCityId())
                .addNew(event.getBuyer().isAddNew())
                .build();

        log.info("addOrEditBuyerCommand: {} ", addOrEditBuyerCommand);


        commandGateway.send(addOrEditBuyerCommand, (commandMessage, commandResultMessage) -> {

            if (commandResultMessage.isExceptional()) {
                // start compensating transaction

            } else {

                log.info("commandMessage: {}", commandMessage);
                log.info("commandResultMessage: {}", commandResultMessage.getPayload());

                if (commandResultMessage.getPayload() != null)
                    factorEntity.setBuyerId(commandResultMessage.getPayload().toString());
            }
        });

        log.info("factorEntity:::::::::: {}", factorEntity);


        try {
            FactorEntity factor = factorRepository.save(factorEntity);
            log.info("factorRepository.save: {}", factor);
            log.info("event.getItems() {}", event.getItems());

            List<FactorItemModel> items = event.getItems();
            items.forEach(item -> {

                if (item.getProduct()!=null){
                    ProductEntity productEntity = new ProductEntity(item.getProduct());
                    productEntity.setProductId(UUID.randomUUID().toString());////////

                    UnitEntity unitEntity = new UnitEntity();
                    unitEntity.setUnitId("121212");//////// unit repository
                    unitEntity.setName("sds");//////// unit repository
                    unitEntity = unitRepository.save(unitEntity);
                    log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@ {}", unitEntity);


                    productEntity.setUnit(unitEntity);
                    log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@productEntity {}", productEntity);
                    productEntity = productRepository.save(productEntity);
                    log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@productEntity {}", productEntity);
                    item.setProduct(new ProductModel(productEntity));
                }


                item.setFactorItemId(UUID.randomUUID().toString());
                item.setFactor(new FactorModel(factor,""));

                log.info("item {}", item);



            });
            List<FactorItemEntity> factorItems = items.stream().map(FactorItemEntity::new).collect(Collectors.toList());
//            List<FactorItemEntity> factorItems = items.stream().map(itemModel -> {
//
//                FactorItemEntity factorItemEntity = new FactorItemEntity();
//
//                return factorItemEntity;
//
//            }).collect(Collectors.toList());

//            factorItems.forEach(item->{});
            log.info("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS::: {}", factorItems);
            factorItems = factorItemRepository.saveAll(factorItems);
            log.info("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS::: {}", factorItems);


        } catch (Exception e) {
            log.info("Exception::: {}", e);

        }

    }


    @EventHandler
    public void on(FactorFilteredEvent event) {

        log.info("@EventHandler called FactorFilteredEvent !!!!!!!!!!!!: {}", event);


    }
}
