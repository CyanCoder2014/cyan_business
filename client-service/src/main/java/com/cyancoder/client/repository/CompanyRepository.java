package com.cyancoder.client.repository;

import com.cyancoder.client.entity.ClientEntity;
import com.cyancoder.client.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<CompanyEntity,String> {

    CompanyEntity findFirstByCompanyId(String companyId);
    CompanyEntity findFirstByNationalCode(Long nationalCode);
    CompanyEntity findFirstByUniqueCode(String uniqueCode);


}
