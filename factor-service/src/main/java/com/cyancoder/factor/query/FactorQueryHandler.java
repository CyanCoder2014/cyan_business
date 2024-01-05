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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactorQueryHandler {

    private final FactorRepository factorRepository;

    private final QueryGateway queryGateway;


    @QueryHandler
    public List<FactorModel> filterFactor(FilterFactorQuery query) throws ParseException {

        List<FactorModel> factors = new ArrayList<>();


        List<FactorEntity> storedFactors = new ArrayList<>();

        if (query.getFactorId() != null)
            storedFactors = factorRepository.findByCompanyIdAndFactorId(query.getCompanyId(),
                    query.getFactorId());
        if (query.getCodeFrom() != null && query.getCodeFrom()!= null)
            storedFactors = factorRepository.findByCompanyIdAndCodeBetween(query.getCompanyId(),
                    query.getCodeFrom(),query.getCodeTo());
        else if (query.getFromDate() != null && query.getToDate()!= null){

            String fromDateStr = query.getCodeFrom() != "" ? query.getCodeFrom() : "2023-05-01";   // need to consider
            SimpleDateFormat fromDateObj = new SimpleDateFormat("yyyy-MM-dd");
            Date fromDate = fromDateObj.parse(fromDateStr);

            String toDateStr = query.getCodeTo() != "" ? query.getCodeTo() : "2025-06-01";   // need to consider
            SimpleDateFormat toDateObj = new SimpleDateFormat("yyyy-MM-dd");
            Date toDate = toDateObj.parse(toDateStr);

            storedFactors = factorRepository.findByCompanyIdAndCreatedAtBetween(query.getCompanyId(),
                    fromDate,toDate);
        }



        storedFactors.forEach(item -> {
            FactorModel factorModel = new FactorModel(item);

            if (factorModel.getBuyer() != null) {
                GetBuyerQuery getBuyerQuery = GetBuyerQuery.builder()
                        .factorId(factorModel.getFactorId())
                        .buyer(factorModel.getBuyer())
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


        FetchBuyerQuery fetchBuyerQuery = new FetchBuyerQuery(query.getBuyer().getBuyerId());

        Buyer buyer = null;

        try {
            buyer = queryGateway.query(fetchBuyerQuery,
                    ResponseTypes.instanceOf(Buyer.class)).join();

        } catch (Exception e) {

            log.error("Exception: {}" + e);

        }
        return buyer;

    }


}
