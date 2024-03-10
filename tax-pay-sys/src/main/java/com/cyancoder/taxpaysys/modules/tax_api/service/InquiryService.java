package com.cyancoder.taxpaysys.modules.tax_api.service;


;
import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.service.InquiryClientService;
import com.cyancoder.taxpaysys.modules.tax_api.entity.FactorTaxEntity;
import com.cyancoder.taxpaysys.modules.tax_api.model.FactorModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.FactorTaxModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.inquiry.InquiryResponseModel;
import com.cyancoder.taxpaysys.modules.tax_api.repository.FactorTaxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryService {

    private final InquiryClientService inquiryClientService;
    private final FactorTaxRepository factorTaxRepository;


    public Object getInquiryByUid(String uniqueCode, String companyId, String uid) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        Object responce; /////// need to consider
        responce = inquiryClientService.getInquiryByUid(uniqueCode, companyId, uid);

        log.info("getInquiryByUid responce: {}",responce);

//        try {
//            Optional<FactorTaxEntity> factorTaxEntity = factorTaxRepository.findByTaxApiUid(uid);
//
//            if (factorTaxEntity.isPresent()) {
//                FactorTaxEntity factor = factorTaxEntity.get();
//                if (responce.successResponse.get(0).status.equals("SUCCESS"))
//                    factor.setSuccessesAt(new Date());
//                factor.setTaxApiMessage(responce.successResponse.get(0).data.toString());
//                factorTaxRepository.save(factor);
//            }
//        } catch (Exception ignored) {}

        return responce;
    }


    public Object getInquiryByReferenceNumber(String uniqueCode, String companyId, String reference) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        Object responce = null; /////// need to consider
        responce = inquiryClientService.getInquiryByReferenceNumber(uniqueCode, companyId, reference);

//        try {
//            Optional<FactorTaxEntity> factorTaxEntity = factorTaxRepository.findByTaxApiReference(reference);
//
//            if (factorTaxEntity.isPresent()) {
//                FactorTaxEntity factor = factorTaxEntity.get();
//                if (responce.successResponse.result.status.equals("SUCCESS"))
//                    factor.setSuccessesAt(new Date());
//                factor.setTaxApiMessage(responce.successResponse.result.data.toString());
//                factorTaxRepository.save(factor);
//            }
//        } catch (Exception ignored) {}

        return responce;
    }


    public List<FactorTaxEntity> getReferences(String factorId) {

        return factorTaxRepository.findByFactorId(factorId);
    }


}
