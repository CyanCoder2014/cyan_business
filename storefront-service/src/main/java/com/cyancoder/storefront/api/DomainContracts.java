package com.cyancoder.storefront.api;
import jakarta.validation.constraints.NotBlank;
public final class DomainContracts {private DomainContracts(){} public record CreateDomainRequest(@NotBlank String domainName,String environment,String redirectTarget){} public record DnsInstruction(String type,String name,String value){} }
