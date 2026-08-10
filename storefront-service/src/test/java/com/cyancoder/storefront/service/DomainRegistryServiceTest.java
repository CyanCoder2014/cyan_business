package com.cyancoder.storefront.service;

import com.cyancoder.storefront.api.DomainContracts.CreateDomainRequest;
import com.cyancoder.storefront.model.DomainBindingEntity;
import com.cyancoder.storefront.repository.DomainBindingRepository;
import com.cyancoder.storefront.repository.DomainEventRepository;
import com.cyancoder.storefront.repository.SiteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DomainRegistryServiceTest {
    private final DomainBindingRepository domains=mock(DomainBindingRepository.class); private final DomainEventRepository events=mock(DomainEventRepository.class); private final SiteRepository sites=mock(SiteRepository.class); private final TenantMembershipClient memberships=mock(TenantMembershipClient.class); private final DomainRegistryService service=new DomainRegistryService(domains,events,sites,memberships);
    @Test void createsPendingDomainWithoutPretendingCertificateSuccess(){when(sites.existsByTenantKeyAndSiteKey("tenant","site")).thenReturn(true);when(domains.save(any())).thenAnswer(i->i.getArgument(0));DomainBindingEntity result=service.create("tenant","site","actor",new CreateDomainRequest("Shop.Company.TLD","PRODUCTION",null));assertEquals("shop.company.tld",result.getDomainName());assertEquals("PENDING",result.getVerificationStatus());assertEquals("NOT_CONFIGURED",result.getCertificateStatus());assertTrue(result.getVerificationToken().startsWith("cyan-verification="));verify(events).save(any());}
    @Test void rejectsInvalidDomainBeforePersistence(){when(sites.existsByTenantKeyAndSiteKey("tenant","site")).thenReturn(true);assertThrows(ResponseStatusException.class,()->service.create("tenant","site","actor",new CreateDomainRequest("not a domain","PRODUCTION",null)));verify(domains,never()).save(any());}
}
