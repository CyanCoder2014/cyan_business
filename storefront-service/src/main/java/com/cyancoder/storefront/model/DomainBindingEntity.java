package com.cyancoder.storefront.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="site_domains", uniqueConstraints=@UniqueConstraint(name="uk_site_domain_name", columnNames="domain_name"))
public class DomainBindingEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="tenant_key",nullable=false,length=120) private String tenantKey;
    @Column(name="site_key",nullable=false,length=120) private String siteKey;
    @Column(name="domain_name",nullable=false,length=253) private String domainName;
    @Column(nullable=false,length=24) private String environment="PRODUCTION";
    @Column(name="verification_token",nullable=false,length=180) private String verificationToken;
    @Column(name="verification_status",nullable=false,length=32) private String verificationStatus="PENDING";
    @Column(name="certificate_status",nullable=false,length=32) private String certificateStatus="NOT_CONFIGURED";
    @Column(name="redirect_target",length=500) private String redirectTarget;
    @Column(name="last_checked_at") private Instant lastCheckedAt;
    @Column(name="created_at",nullable=false) private Instant createdAt=Instant.now();
    @Column(name="updated_at",nullable=false) private Instant updatedAt=Instant.now();
    protected DomainBindingEntity() {}
    public DomainBindingEntity(String tenantKey,String siteKey,String domainName,String environment,String verificationToken,String redirectTarget){this.tenantKey=tenantKey;this.siteKey=siteKey;this.domainName=domainName;this.environment=environment;this.verificationToken=verificationToken;this.redirectTarget=redirectTarget;}
    public Long getId(){return id;} public String getTenantKey(){return tenantKey;} public String getSiteKey(){return siteKey;} public String getDomainName(){return domainName;} public String getEnvironment(){return environment;} public String getVerificationToken(){return verificationToken;} public String getVerificationStatus(){return verificationStatus;} public String getCertificateStatus(){return certificateStatus;} public String getRedirectTarget(){return redirectTarget;} public Instant getLastCheckedAt(){return lastCheckedAt;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
    public void setVerificationStatus(String value){verificationStatus=value;} public void setLastCheckedAt(Instant value){lastCheckedAt=value;} public void setUpdatedAt(Instant value){updatedAt=value;}
}
