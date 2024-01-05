package com.cyancoder.client.query;


import com.cyancoder.client.entity.ClientEntity;
import com.cyancoder.client.entity.CompanyEntity;
import com.cyancoder.client.model.ClientModel;
import com.cyancoder.client.model.CompanyModel;
import com.cyancoder.client.repository.ClientRepository;
import com.cyancoder.client.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyQueryHandler {

    private final CompanyRepository companyRepository;

    @QueryHandler
    public CompanyModel getCompany(FilterCompanyQuery query) {

//        CompanyModel company = new CompanyModel();

        CompanyEntity storedCompany = new CompanyEntity();
        if (query.getCompanyId() != null)
            storedCompany = companyRepository.findFirstByCompanyId(query.getCompanyId());
        else if (query.getNationalCode() != null)
            storedCompany = companyRepository.findFirstByNationalCode(query.getNationalCode());
        else if (query.getUniqueCode() != null)
            storedCompany = companyRepository.findFirstByUniqueCode(query.getUniqueCode());//////////


        CompanyModel model = new CompanyModel(storedCompany);

//        List<CompanyEntity> storedCompany = companyRepository.findAll();
//        storedCompany.forEach(item -> {
//            CompanyModel model = new CompanyModel(item);
//            company.add(model);
//        });

        return model;

    }


//    @QueryHandler
//    public CompanyModel filterClient(FilterCompanyQuery query) {
//
//        CompanyModel company = new CompanyModel();
//
//        CompanyEntity storedCompany = new CompanyEntity();
//        if (query.getCompanyId() != null)
//            storedCompany = companyRepository.findByCompanyId(query.getCompanyId());
//        else if (query.getNationalCode() != null)
//            storedCompany = companyRepository.findByNationalCode(query.getNationalCode());
//        else if (query.getUniqueCode() != null)
//            storedCompany = companyRepository.findByUniqueCode(query.getUniqueCode());//////////
//
//
//        CompanyModel model = new CompanyModel(storedCompany);
//
////        List<CompanyEntity> storedCompany = companyRepository.findAll();
////        storedCompany.forEach(item -> {
////            CompanyModel model = new CompanyModel(item);
////            company.add(model);
////        });
//
//        return company;
//
//    }


}
