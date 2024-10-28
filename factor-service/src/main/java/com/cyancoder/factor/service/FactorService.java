package com.cyancoder.factor.service;

import com.cyancoder.factor.client.services_api.entity.FactorTaxEntity;
import com.cyancoder.factor.client.services_api.service.TaxClientService;
import com.cyancoder.factor.command.CreateFactorCommand;
import com.cyancoder.factor.entity.FactorEntity;
import com.cyancoder.factor.entity.FactorItemEntity;
import com.cyancoder.factor.entity.ProductEntity;
import com.cyancoder.factor.entity.UnitEntity;
import com.cyancoder.factor.model.*;
import com.cyancoder.factor.model.request.CreateFactorReqModel;
import com.cyancoder.factor.model.request.RequestTaxModel;
import com.cyancoder.factor.model.request.UpdateFactorReqModel;
import com.cyancoder.factor.repository.FactorItemRepository;
import com.cyancoder.factor.repository.FactorRepository;
import com.cyancoder.factor.repository.ProductRepository;
import com.cyancoder.factor.repository.UnitRepository;
import com.cyancoder.generic.command.buyer.AddOrEditBuyerCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.x.protobuf.MysqlxCrud;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class FactorService {

    private final FactorRepository factorRepository;
    private final FactorItemRepository factorItemRepository;
    private final ProductRepository productRepository;
    private final UnitRepository unitRepository;

    private  final Environment env;
    private  final CommandGateway commandGateway;
    private final TaxClientService taxClientService;


    public Object addFactor(CreateFactorReqModel createFactorReqModel) throws ParseException {


        String factorDateStr = createFactorReqModel.getFactorDate();   // to consider
        SimpleDateFormat fromDateObj = new SimpleDateFormat("yyyy-MM-dd");
        Date actorDate = fromDateObj.parse(factorDateStr);

        CreateFactorCommand createFactorCommand = CreateFactorCommand.builder()
                .factorId(UUID.randomUUID().toString())
                .code(createFactorReqModel.getCode())
                .factorDate(actorDate)
                .payType(createFactorReqModel.getPayType())
                .state(createFactorReqModel.getState())
                .note(createFactorReqModel.getNote())
                .items(createFactorReqModel.getItems())
                .companyId(createFactorReqModel.getCompanyId())
                .buyer(createFactorReqModel.getBuyer())

                .type(createFactorReqModel.getType())
                .pattern(createFactorReqModel.getPattern())
                .contractId(createFactorReqModel.getContractId())

                .build();


        Object response = null;

        FactorEntity factorEntity = new FactorEntity();
        BeanUtils.copyProperties(createFactorCommand, factorEntity);
        if (factorEntity.getCode() == null) {
            long lastCode;
            try {
                lastCode = Long.parseLong(factorRepository.findTopByCompanyIdOrderByCodeDesc(createFactorCommand.getCompanyId()).getCode());
            } catch (Exception e) {
                lastCode = 1000000000;
            }

            factorEntity.setCode(String.valueOf(lastCode + 1));
        }
        log.info("temp: {}", createFactorCommand);


        ////************* before send data to gateway need to check buyer availability ************/////
//        BuyerEntity buyerEntityOld = buyerRepository.findFirstByNationalCode(event.getNationalCode());
//        BuyerEntity buyerEntityOld = buyerRepository.findByBuyerId(event.getBuyerId());
//        if (!event.isAddNew() && buyerEntityOld != null){
//            buyerEntity.setBuyerId(buyerEntityOld.getBuyerId());
//        }
        AddOrEditBuyerCommand addOrEditBuyerCommand = AddOrEditBuyerCommand.builder()
                .buyerId(createFactorCommand.getBuyer().getBuyerId() != null ? createFactorCommand.getBuyer().getBuyerId() : UUID.randomUUID().toString())
                .nationalCode(createFactorCommand.getBuyer().getNationalCode())
                .economicCode(createFactorCommand.getBuyer().getEconomicCode())
                .buyerType(createFactorCommand.getBuyer().getBuyerType())
                .tell(createFactorCommand.getBuyer().getTell())
                .address(createFactorCommand.getBuyer().getAddress())
                .postCode(createFactorCommand.getBuyer().getPostCode())
                .cityId(createFactorCommand.getBuyer().getCityId())
                .addNew(createFactorCommand.getBuyer().isAddNew())
                .build();

        log.info("temp: {} ", addOrEditBuyerCommand);


        String id = commandGateway.sendAndWait(addOrEditBuyerCommand);
        factorEntity.setBuyerId(id);


        try {
            FactorEntity factor = factorRepository.save(factorEntity);
            log.info("factorRepository.save: {}", factor);
            log.info("temp --.getItems() {}", createFactorCommand.getItems());

            List<FactorItemModel> items = createFactorCommand.getItems();
            items.forEach(item -> {


                if (item.getProduct() != null) {
                    ProductEntity productEntity = new ProductEntity(item.getProduct());


                    if (item.getProduct().getProductId() != null)
                        productEntity.setProductId(item.getProduct().getProductId());
                    else
                        productEntity.setProductId(UUID.randomUUID().toString());

                    UnitEntity unitEntity = new UnitEntity();
                    if (item.getProduct().getUnit() != null) {
                        if (item.getProduct().getUnit().getUnitId() != null)
                            unitEntity.setUnitId(item.getProduct().getUnit().getUnitId());
                        else
                            unitEntity.setUnitId(UUID.randomUUID().toString());


                        unitEntity.setName(item.getProduct().getUnit().getName());
                        unitEntity.setCode(item.getProduct().getUnit().getCode());
                        unitEntity = unitRepository.save(unitEntity);
                    }

                    productEntity.setUnit(unitEntity);
                    productEntity = productRepository.save(productEntity);
                    item.setProduct(new ProductModel(productEntity));
                }


                item.setFactorItemId(UUID.randomUUID().toString());
                item.setFactor(new FactorModel(factor, ""));


            });
            List<FactorItemEntity> factorItems = items.stream().map(FactorItemEntity::new).collect(Collectors.toList());

            response = factorItemRepository.saveAll(factorItems);


        } catch (Exception e) {
            log.info("Exception::: {}", e);

        }


        return response;
    }


    public Object editFactor(UpdateFactorReqModel updateFactorReqModel) throws Exception {
        String factorId = updateFactorReqModel.getFactorId();

        List<FactorTaxEntity> factorTaxEntities = taxClientService.getReferences(new RequestTaxModel(factorId));
        if (!factorTaxEntities.stream().filter(i-> !ObjectUtils.isEmpty(i.getSuccessesAt())).toList().isEmpty()) {
            throw new Exception("Can not Update Factor!");
        }

        factorRepository.deleteById(factorId);
        ObjectMapper mapper = new ObjectMapper();
        CreateFactorReqModel createFactorReqModel = mapper.convertValue(updateFactorReqModel, CreateFactorReqModel.class);
        return this.addFactor(createFactorReqModel);
    }


    public String removeFactor(String factorId) throws Exception {
        List<FactorTaxEntity> factorTaxEntities = taxClientService.getReferences(new RequestTaxModel(factorId));

        if (!factorTaxEntities.stream().filter(i-> !ObjectUtils.isEmpty(i.getSuccessesAt())).toList().isEmpty()) {
            throw new Exception("Can not Remove Factor!");
        }
        factorRepository.deleteById(factorId);
        return factorId;
    }


    public String removeFactorByCode(String factorCode) {
        factorRepository.deleteAllByCode(factorCode);
        return factorCode;
    }


    public Page<FactorEntity> getFactorByFilter(FactorFilterModel filter, PageableModel pageableModel)  {

        Pageable pageable = PageRequest.of(pageableModel.page(), pageableModel.pageSize(),
                Sort.by(PageableModel.SortOrder.ASC.equals(pageableModel.sortOrder()) ? Sort.Direction.ASC: Sort.Direction.DESC ,
                        pageableModel.sortKey()));


        Page<FactorEntity> pageableFactors = null;

        if (!ObjectUtils.isEmpty(filter.codeFrom()) && !ObjectUtils.isEmpty(filter.codeTo()))
            pageableFactors = factorRepository.findByCompanyIdAndCodeBetween(filter.companyId(),
                    filter.codeFrom(),filter.codeTo(),pageable);
        else
            pageableFactors = factorRepository.findByCompanyIdAndCreatedAtBetween(filter.companyId(),
                    filter.startDate(),filter.endDate(),pageable);

        return pageableFactors;

//        return factorRepository.findByCompanyIdAndCreatedAtBetweenAndCodeBetween(filter.companyId(),
//                filter.startDate(),  filter.endDate(),
//                 filter.codeFrom(),  filter.codeTo(),
//                pageable);
    }

}
