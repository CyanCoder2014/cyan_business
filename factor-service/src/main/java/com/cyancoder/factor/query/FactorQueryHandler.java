package com.cyancoder.factor.query;


import com.cyancoder.factor.entity.FactorEntity;
import com.cyancoder.factor.model.BuyerModel;
import com.cyancoder.factor.model.FactorModel;
import com.cyancoder.factor.repository.FactorRepository;
import com.cyancoder.generic.model.Buyer;
import com.cyancoder.generic.query.FetchBuyerQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactorQueryHandler {

    private final FactorRepository factorRepository;

    private final CommandGateway commandGateway;/////////////////////

    private final QueryGateway queryGateway;


    @QueryHandler
    public List<FactorModel> filterFactor(FilterFactorQuery query) {

        List<FactorModel> factors = new ArrayList<>();

        List<FactorEntity> storedFactors = factorRepository.findAll();

        storedFactors.forEach(item -> {
            FactorModel factorModel = new FactorModel(item);


            if (factorModel.getBuyerId() != null) {

//                SetBuyerRelationCommand setBuyerRelationCommand = SetBuyerRelationCommand.builder()
//                        .factorId(UUID.randomUUID().toString())
//                        .code(factorModel.getCode())
//                        .buyerId(factorModel.getBuyerId())
//                        .build();
//                try {
//                    commandGateway.sendAndWait(setBuyerRelationCommand);
//                } catch (Exception e) {}


                GetBuyerQuery getBuyerQuery = GetBuyerQuery.builder()
                        .factorId(factorModel.getFactorId())
                        .buyerId(factorModel.getBuyerId())
                        .build();
                Buyer buyer = queryGateway.query(getBuyerQuery,
                        ResponseTypes.instanceOf(Buyer.class)).join();

                BuyerModel model = new BuyerModel();
                if (buyer!=null){
                    BeanUtils.copyProperties(buyer, model);
                    factorModel.setBuyer(model);
                }

            }


            factors.add(factorModel);
        });


        return factors;

    }


    @QueryHandler
    public Buyer getBuyer(GetBuyerQuery query) {

        log.info("@QueryHandler called getBuyer !!!!!!!!!!!!!!: {}", query);

        FetchBuyerQuery fetchBuyerQuery = new FetchBuyerQuery(query.getBuyerId());

        Buyer buyer = null;

        try {
            buyer = queryGateway.query(fetchBuyerQuery,
                    ResponseTypes.instanceOf(Buyer.class)).join();

        } catch (Exception e) {

            log.error("eeeeeeeee: {}" + e);

        }
        return buyer;

    }


}
