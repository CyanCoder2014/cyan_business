# Cyan Business Microservices Analysis

## Purpose

This document is a transfer-oriented technical analysis of the `cyan_business` repository.
It is written for future engineers or AI agents who need to:

- understand the microservices in this project
- identify the real business capabilities behind those services
- decide what should be migrated into `naviya`
- avoid copying weak or broken parts of the current implementation

This file is based on code inspection of the repository, not on README documentation.
Most service READMEs are placeholders and are not useful as architecture references.

## Repository Summary

This repository is a Java 17 / Spring Boot 3 multi-module Gradle project with these modules:

- `discovery-server`
- `api-gateway`
- `buyer-service`
- `client-service`
- `factor-service`
- `product-service`
- `tax-pay-sys`
- `generic`

Reference:

- [settings.gradle](/Users/farid/Projects/naviya/old-cyan/cyan_business/settings.gradle:1)

At runtime the system expects:

- MySQL databases, usually one schema per service
- Eureka service discovery
- Axon Server for CQRS/event processing
- Keycloak for JWT authentication

References:

- [discovery-server/src/main/resources/application.properties](/Users/farid/Projects/naviya/old-cyan/cyan_business/discovery-server/src/main/resources/application.properties:1)
- [api-gateway/src/main/resources/application.properties](/Users/farid/Projects/naviya/old-cyan/cyan_business/api-gateway/src/main/resources/application.properties:1)
- [factor-service/src/main/resources/application.properties](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/resources/application.properties:1)
- [tax-pay-sys/src/main/resources/application.properties](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/resources/application.properties:1)
- [docker/axon/docker-compose.yml](/Users/farid/Projects/naviya/old-cyan/cyan_business/docker/axon/docker-compose.yml:1)
- [docker/keycloak/docker-compose.yml](/Users/farid/Projects/naviya/old-cyan/cyan_business/docker/keycloak/docker-compose.yml:1)

## High-Level Architecture

The project is a mix of:

- infrastructure services
- simple CRUD-style business services
- Axon-based CQRS/event-driven persistence
- one specialized external tax integration service

The important distinction is that the repository contains two different layers of complexity:

1. Basic domain services for company, buyer, product, and factor/invoice data
2. A specialized tax submission pipeline for the Iranian tax API

The current microservice split is partly organizational. The domain model itself is not very complex and could be collapsed into fewer deployable units without losing business meaning.

## Service Inventory

### `discovery-server`

Role:

- Netflix Eureka registry
- central service discovery point

Key characteristics:

- runs on port `8761`
- services register against it
- no business logic

References:

- [discovery-server/src/main/java/com/cyancoder/discoveryserver/DiscoveryServerApplication.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/discovery-server/src/main/java/com/cyancoder/discoveryserver/DiscoveryServerApplication.java:7)
- [discovery-server/src/main/resources/application.properties](/Users/farid/Projects/naviya/old-cyan/cyan_business/discovery-server/src/main/resources/application.properties:1)

Migration value:

- low
- should not be migrated into `naviya` unless `naviya` already uses Spring Cloud service discovery

### `api-gateway`

Role:

- Spring Cloud Gateway entry point
- route forwarding to internal services
- JWT resource server

Key characteristics:

- runs on port `8001`
- requires JWT for most routes
- forwards requests to service IDs via Eureka
- exposes Keycloak realm path

References:

- [api-gateway/src/main/resources/application.properties](/Users/farid/Projects/naviya/old-cyan/cyan_business/api-gateway/src/main/resources/application.properties:1)
- [api-gateway/src/main/java/come/cyancoder/apigateway/config/SecurityConfig.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/api-gateway/src/main/java/come/cyancoder/apigateway/config/SecurityConfig.java:16)

Important note:

- this gateway still contains routes for services not present in this repository:
  - `seller-service`
  - `city-service`
  - `order-service`

Those route definitions suggest the repo is an incomplete slice of a larger ecosystem.

Migration value:

- low to medium
- route patterns and auth expectations are informative
- the gateway implementation itself should not be copied by default

### `client-service`

Role:

- manages client and company data
- stores company identity data used by the tax module
- stores company-specific secrets needed for signing

Key business capability:

- company onboarding and identity binding to the authenticated JWT client

Important create behavior:

- generates `companyId`
- reads `client_id` from JWT
- stores:
  - company name
  - national code
  - economic code
  - hashed `uniqueCode`
  - encrypted private key `pk`

References:

- [client-service/src/main/java/com/cyancoder/client/rest/CompanyCommandController.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/client-service/src/main/java/com/cyancoder/client/rest/CompanyCommandController.java:21)
- [client-service/src/main/java/com/cyancoder/client/entity/CompanyEntity.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/client-service/src/main/java/com/cyancoder/client/entity/CompanyEntity.java:15)
- [client-service/src/main/java/com/cyancoder/client/query/CompanyQueryHandler.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/client-service/src/main/java/com/cyancoder/client/query/CompanyQueryHandler.java:21)
- [client-service/src/main/java/com/cyancoder/client/repository/CompanyRepository.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/client-service/src/main/java/com/cyancoder/client/repository/CompanyRepository.java:7)

Why it matters:

- this is the real tenant/company identity service in the repo
- `tax-pay-sys` depends on it to retrieve company-level credentials and validate ownership

Migration value:

- high
- one of the strongest migration candidates for `naviya`

### `buyer-service`

Role:

- manages buyer/customer records
- provides buyer lookup for factor/invoice flows

Key characteristics:

- buyer records are lightweight
- service is used as supporting master data for factors
- also acts as query responder for `FetchBuyerQuery`

References:

- [buyer-service/src/main/java/com/cyancoder/client/entity/BuyerEntity.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/buyer-service/src/main/java/com/cyancoder/client/entity/BuyerEntity.java:11)
- [buyer-service/src/main/java/com/cyancoder/client/repository/BuyerRepository.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/buyer-service/src/main/java/com/cyancoder/client/repository/BuyerRepository.java:6)
- [buyer-service/src/main/java/com/cyancoder/client/query/BuyerQueryHandler.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/buyer-service/src/main/java/com/cyancoder/client/query/BuyerQueryHandler.java:19)
- [buyer-service/src/main/java/com/cyancoder/client/query/BuyerEventHandler.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/buyer-service/src/main/java/com/cyancoder/client/query/BuyerEventHandler.java:15)

Why it matters:

- `factor-service` depends on the buyer domain
- but the buyer model is simple enough that it does not justify a separate deployable service unless organizational ownership requires it

Migration value:

- medium
- useful domain data, but better merged into a billing/customer module in `naviya`

### `factor-service`

Role:

- manages factors, effectively invoices or bill headers
- manages factor items
- creates related buyer/product/unit read models during factor persistence

This is the main billing/invoice service.

Core endpoints:

- create factor
- query factors by company, code range, date range, factor ID

References:

- [factor-service/src/main/java/com/cyancoder/factor/rest/FactorCommandController.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/rest/FactorCommandController.java:17)
- [factor-service/src/main/java/com/cyancoder/factor/rest/FactorQueryController.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/rest/FactorQueryController.java:22)
- [factor-service/src/main/java/com/cyancoder/factor/query/FactorEventHandler.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/query/FactorEventHandler.java:32)
- [factor-service/src/main/java/com/cyancoder/factor/query/FactorQueryHandler.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/query/FactorQueryHandler.java:25)
- [factor-service/src/main/java/com/cyancoder/factor/entity/FactorEntity.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/entity/FactorEntity.java:15)
- [factor-service/src/main/java/com/cyancoder/factor/entity/FactorItemEntity.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/entity/FactorItemEntity.java:19)
- [factor-service/src/main/java/com/cyancoder/factor/entity/ProductEntity.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/entity/ProductEntity.java:18)
- [factor-service/src/main/java/com/cyancoder/factor/repository/FactorRepository.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/repository/FactorRepository.java:10)

Why it matters:

- this is the richest business domain in the repo
- the tax module relies on this service as the source of invoice data for submission

Migration value:

- very high
- this is the most important business feature to carry into `naviya` if invoice/factor functionality is needed

### `product-service`

Role:

- intended to manage product master data

Reality:

- currently incomplete
- create controller builds an empty command and does not map request fields

References:

- [product-service/src/main/java/com/cyancoder/client/rest/ProductCommandController.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/product-service/src/main/java/com/cyancoder/client/rest/ProductCommandController.java:11)
- [product-service/src/main/java/com/cyancoder/client/query/ProductEventHandler.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/product-service/src/main/java/com/cyancoder/client/query/ProductEventHandler.java:15)

Migration value:

- low as-is
- use only as a weak domain hint, not as copy-ready code

### `tax-pay-sys`

Role:

- specialized tax submission integration service
- fetches factor and company data from internal services
- transforms factors into tax invoice payloads
- signs and encrypts packets
- calls the external tax API
- stores submission metadata

This is the most specialized module in the repository.

References:

- [tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/rest/InvoiceController.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/rest/InvoiceController.java:12)
- [tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/service/FactorService.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/service/FactorService.java:48)
- [tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/client/services_api/service/CompanyClientService.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/client/services_api/service/CompanyClientService.java:14)
- [tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/client/services_api/service/FactorClientService.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/client/services_api/service/FactorClientService.java:22)
- [tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/entity/FactorTaxEntity.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/entity/FactorTaxEntity.java:13)

Why it matters:

- if `naviya` needs Iranian tax invoice submission, this module is the main transfer source
- if not, this service can be ignored almost entirely

Migration value:

- high only if the tax integration is required
- otherwise low

### `generic`

Role:

- shared Axon command/query/event contracts used across services

Key example:

- buyer add/edit command used from `factor-service` to `buyer-service`

References:

- [generic/src/main/java/com/cyancoder/generic/command/buyer/AddOrEditBuyerCommand.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/generic/src/main/java/com/cyancoder/generic/command/buyer/AddOrEditBuyerCommand.java:1)
- [generic/src/main/java/com/cyancoder/generic/query/FetchBuyerQuery.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/generic/src/main/java/com/cyancoder/generic/query/FetchBuyerQuery.java:1)
- [generic/src/main/java/com/cyancoder/generic/model/Buyer.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/generic/src/main/java/com/cyancoder/generic/model/Buyer.java:1)

Migration value:

- low by itself
- useful only if continuing with the Axon architecture

## Business Capability Map

The best way to understand this repository is by capability, not by service name.

### 1. Company Identity and Signing Data

Owned mainly by:

- `client-service`

What it includes:

- company record
- client ownership via JWT `client_id`
- company national/economic identifiers
- `uniqueCode`
- private signing key storage

This capability is essential for the tax integration because it determines:

- who owns a company
- which credentials belong to that company
- whether tax actions are authorized for that company

### 2. Buyer Registry

Owned mainly by:

- `buyer-service`

What it includes:

- buyer/customer identifiers
- buyer national/economic data
- address and postal data
- buyer type

This capability is not complex. It looks like supporting master data, not a standalone strategic domain.

### 3. Factor / Invoice Domain

Owned mainly by:

- `factor-service`

What it includes:

- factor header
- factor items
- product references
- unit references
- company linkage
- buyer linkage
- basic filter queries

This is the core domain for business billing behavior.

### 4. Tax Submission Pipeline

Owned mainly by:

- `tax-pay-sys`

What it includes:

- factor fetch
- company fetch
- invoice DTO construction
- tax ID generation
- cryptographic signing
- encryption
- packet transfer
- submission tracking

This is a technical integration layer built on top of the factor and company domains.

## Data Flow Between Services

## Factor Creation Flow

Main flow:

1. client calls `factor-service` create endpoint
2. controller builds `CreateFactorCommand`
3. Axon handles command and emits `FactorCreatedEvent`
4. `FactorEventHandler`:
   - generates factor code if missing
   - sends `AddOrEditBuyerCommand`
   - stores factor
   - stores factor items
   - persists product/unit read-model data when embedded in the request

References:

- [factor-service/src/main/java/com/cyancoder/factor/rest/FactorCommandController.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/rest/FactorCommandController.java:28)
- [factor-service/src/main/java/com/cyancoder/factor/query/FactorEventHandler.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/query/FactorEventHandler.java:52)

Important interpretation:

- factor creation is the central business action in this project
- buyer data is subordinate to factor creation

## Factor Query Flow

Main flow:

1. client calls `GET /v2/api/factor-service/factors`
2. controller converts request params into `FilterFactorQuery`
3. Axon query handler loads `FactorEntity` rows
4. query handler resolves buyer details through `FetchBuyerQuery`
5. returns assembled factor models

References:

- [factor-service/src/main/java/com/cyancoder/factor/rest/FactorQueryController.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/rest/FactorQueryController.java:33)
- [factor-service/src/main/java/com/cyancoder/factor/query/FactorQueryHandler.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/query/FactorQueryHandler.java:35)

Important interpretation:

- read-model assembly is distributed across services because of Axon
- in a consolidated `naviya` implementation, this should become a simple local service call or repository join strategy

## Tax Submission Flow

Main flow:

1. client calls `POST /v2/api/tax/invoice/send-invoice`
2. `tax-pay-sys` fetches company through `client-service`
3. `tax-pay-sys` fetches factors through `factor-service`
4. `tax-pay-sys` maps factors to tax invoice DTOs
5. `tax-pay-sys` decrypts the company private key
6. `tax-pay-sys` retrieves tax server public key
7. `tax-pay-sys` signs and encrypts invoice packets
8. `tax-pay-sys` submits to tax API
9. `tax-pay-sys` stores response metadata in `t_factor_tax`

References:

- [tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/rest/InvoiceController.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/rest/InvoiceController.java:46)
- [tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/service/FactorService.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/service/FactorService.java:64)

Important interpretation:

- `tax-pay-sys` is an orchestration layer, not a source-of-truth domain
- it depends on internal data quality from `client-service` and `factor-service`

## Main Persistence Model

The repository’s effective persistence model is:

- `u_company`
- `u_client`
- `b_buyer`
- `f_factors`
- `f_factor_items`
- `f_products`
- `f_unit`
- `t_factor_tax`

The most important entities for migration are:

- [client-service CompanyEntity](/Users/farid/Projects/naviya/old-cyan/cyan_business/client-service/src/main/java/com/cyancoder/client/entity/CompanyEntity.java:15)
- [buyer-service BuyerEntity](/Users/farid/Projects/naviya/old-cyan/cyan_business/buyer-service/src/main/java/com/cyancoder/client/entity/BuyerEntity.java:11)
- [factor-service FactorEntity](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/entity/FactorEntity.java:15)
- [factor-service FactorItemEntity](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/entity/FactorItemEntity.java:19)
- [factor-service ProductEntity](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/entity/ProductEntity.java:18)
- [tax-pay-sys FactorTaxEntity](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/entity/FactorTaxEntity.java:13)

## Security Model

The repository uses Keycloak JWT resource server auth in multiple services.

Common patterns:

- gateway validates JWT
- downstream services also validate JWT
- some services extract token attributes directly from security context
- `client_id` is used as a tenant/ownership boundary

References:

- [api-gateway/src/main/java/come/cyancoder/apigateway/config/SecurityConfig.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/api-gateway/src/main/java/come/cyancoder/apigateway/config/SecurityConfig.java:21)
- [client-service/src/main/java/com/cyancoder/client/config/OAuthToken.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/client-service/src/main/java/com/cyancoder/client/config/OAuthToken.java:15)
- [tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/config/OauthToken.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/config/OauthToken.java:11)

Important interpretation:

- authorization logic is weakly centralized
- the most meaningful business auth rule is company ownership by `client_id`

## Tax Integration Internals

The most reusable technical part of `tax-pay-sys` is the transfer stack.

Main components:

- transfer orchestration:
  - [ObjectTransferApiImpl](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/transfer/api/ObjectTransferApiImpl.java:16)
- encryption:
  - [DefaultEncrypter](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/transfer/impl/encrypter/DefaultEncrypter.java:20)
- signing:
  - [InMemorySignatory](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/transfer/impl/signatory/InMemorySignatory.java:15)

This stack is more reusable than the service layer around it.

If another system needs the same tax API integration, this transfer package is the first thing worth extracting.

## Operational Dependencies

The system relies on several local/static assumptions:

- hardcoded localhost service endpoints
- local MySQL instances
- local Keycloak realm
- local Eureka
- long Feign timeouts

Examples:

- [api-gateway application routes](/Users/farid/Projects/naviya/old-cyan/cyan_business/api-gateway/src/main/resources/application.properties:15)
- [tax-pay-sys service discovery fallback](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/resources/application.properties:46)

This means the system is not cleanly environment-abstracted.

For migration, expect to redesign configuration layout rather than reusing it.

## Major Problems and Code Smells

This section is important for any future AI agent.
Do not assume this repository is production-clean.

### 1. Placeholder READMEs

The service READMEs are mostly GitLab template text and not useful documentation.

### 2. Incomplete Services

`product-service` create logic is unfinished.

Reference:

- [product-service/src/main/java/com/cyancoder/client/rest/ProductCommandController.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/product-service/src/main/java/com/cyancoder/client/rest/ProductCommandController.java:21)

`buyer-service` create logic is also effectively incomplete in the controller layer.

Reference:

- [buyer-service/src/main/java/com/cyancoder/client/rest/BuyerCommandController.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/buyer-service/src/main/java/com/cyancoder/client/rest/BuyerCommandController.java:21)

### 3. Broken Query Logic

`BuyerQueryHandler.filterBuyer` returns an empty list instead of mapped buyers.

Reference:

- [buyer-service/src/main/java/com/cyancoder/client/query/BuyerQueryHandler.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/buyer-service/src/main/java/com/cyancoder/client/query/BuyerQueryHandler.java:23)

`CompanyQueryController` uses `Long.getLong(...)` instead of parsing a numeric string.

Reference:

- [client-service/src/main/java/com/cyancoder/client/rest/CompanyQueryController.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/client-service/src/main/java/com/cyancoder/client/rest/CompanyQueryController.java:37)

### 4. Incorrect String Comparisons

Several places use `==` or `!=` with strings, which is wrong in Java.

Examples:

- [factor-service/src/main/java/com/cyancoder/factor/rest/FactorQueryController.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/rest/FactorQueryController.java:43)
- [factor-service/src/main/java/com/cyancoder/factor/query/FactorQueryHandler.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/factor-service/src/main/java/com/cyancoder/factor/query/FactorQueryHandler.java:52)
- [tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/service/FactorService.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/service/FactorService.java:148)

### 5. Tax Service Logic Bugs

In the factor-code path, `codeTo` is not set correctly.

Reference:

- [tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/service/FactorService.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/service/FactorService.java:75)

Private-key validation is inverted:

- it throws when `pk` is not null, which is almost certainly wrong

Reference:

- [tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/service/FactorService.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/service/FactorService.java:208)

### 6. Route Inconsistency

The gateway routes `tax-pay-sys` through `/v2/api/tax-service/**`, but some tax controllers are under `/v2/api/tax/**`.

References:

- [api-gateway/src/main/resources/application.properties](/Users/farid/Projects/naviya/old-cyan/cyan_business/api-gateway/src/main/resources/application.properties:25)
- [tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/rest/InvoiceController.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/main/java/com/cyancoder/taxpaysys/modules/tax_api/rest/InvoiceController.java:13)

This likely means some tax endpoints are not properly reachable through the gateway.

### 7. Weak Test Coverage

There are almost no meaningful tests.
Only bootstrap-style application tests are present.

Examples:

- [tax-pay-sys/src/test/java/com/cyancoder/cyan_count_v2/CyanCountV2ApplicationTests.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/tax-pay-sys/src/test/java/com/cyancoder/cyan_count_v2/CyanCountV2ApplicationTests.java:1)
- [discovery-server/src/test/java/com/cyancoder/discoveryserver/DiscoveryServerApplicationTests.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/discovery-server/src/test/java/com/cyancoder/discoveryserver/DiscoveryServerApplicationTests.java:1)

## What Should Be Migrated to Naviya

This section is the key recommendation set.

### Best Migration Candidates

#### 1. Company Management and Credential Storage

Move the `client-service` company capability into `naviya`.

Why:

- strong business value
- clear ownership model through `client_id`
- required by the tax submission flow
- conceptually compact

What to carry:

- company entity fields
- unique code hashing
- encrypted private key storage
- company lookup by ownership and ID

#### 2. Factor / Invoice Domain

Move the `factor-service` business rules into `naviya`.

Why:

- central domain in this repo
- required for tax submission
- current implementation already shows useful rules for invoice creation and code assignment

What to carry:

- factor entity
- factor item entity
- buyer association
- company association
- query/filter behavior

#### 3. Buyer Data

Move buyer/customer support, but preferably merge it into the same billing domain instead of keeping a separate microservice.

Why:

- simple data model
- mostly subordinate to factors

#### 4. Tax Transfer and Packet Logic

Move the tax integration only if `naviya` actually needs it.

What to carry:

- invoice DTO mapping ideas
- packet building
- signing
- encryption
- response tracking

Do not carry:

- the full service orchestration structure as-is

## What Should Not Be Copied Directly

Avoid direct transfer of:

- Eureka service discovery
- current Spring Cloud Gateway setup
- Axon CQRS plumbing unless `naviya` already uses Axon
- placeholder/incomplete service code
- static localhost-based configuration

Reason:

- those are implementation choices, not core business assets
- they add complexity without clear migration value

## Recommended Target Architecture for Naviya

If `naviya` does not already require this microservice topology, the better target design is:

- one modular billing/tax domain inside `naviya`

Suggested bounded modules:

- `company`
- `buyer`
- `factor`
- `tax-submission`

Suggested interpretation:

- `company` owns tenant/company identity and signing secrets
- `buyer` owns customer master data
- `factor` owns invoice/factor and items
- `tax-submission` is an integration layer on top of local domain services

This would replace current service-to-service hops with local application service calls.

## Recommended Migration Order

### Phase 1

Move company management first.

Goal:

- establish tenant/company records
- establish secure credential storage

### Phase 2

Move factor/invoice domain.

Goal:

- establish invoice data model
- establish buyer linkage
- establish filters and retrieval

### Phase 3

Merge or move buyer support.

Goal:

- remove dependency on separate buyer-service patterns

### Phase 4

Move tax submission capability.

Goal:

- reuse invoice data from local `naviya` domain
- add signing/encryption and outbound tax API integration

## Guidance for Future AI Agents

If you are another AI agent reading this file, use these rules:

1. Treat this repo as a feature source, not a clean architecture template.
2. Prefer extracting domain concepts over copying service boundaries.
3. Assume Axon can be removed unless the target system already depends on it.
4. Assume `product-service` is incomplete and unreliable.
5. Assume tax integration logic needs review before production use.
6. Preserve the useful business concepts:
   - company ownership
   - secure private key storage
   - factor/invoice domain
   - tax packet/signature/encryption workflow
7. Re-check all string comparison and validation logic before reusing any code.
8. Expect that some route and configuration assumptions in this repo are stale.

## Bottom-Line Conclusion

This repository is best understood as:

- a small billing domain implemented as several Spring services
- plus a specialized tax submission integration layer

The highest-value features for migration into `naviya` are:

- company onboarding and secure key storage
- factor/invoice management
- buyer support
- tax submission packet/signing/encryption logic if needed

The current microservice boundaries, Axon plumbing, and gateway/discovery setup should not be treated as mandatory parts of the transfer.
