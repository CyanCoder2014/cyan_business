# report-service Agent Guide

## Purpose
`report-service` defines and executes dynamic reports across business services.

## Owns
- report definitions and run requests
- template `dynamic-report`

## Main APIs
- report CRUD and run APIs
- internal dynamic report endpoints

## Dependencies
- `content-service`
- `catalog-service`
- `crm-service`
- `commerce-service`
- `finance-service`
- `inventory-service`

## Flow Role
1. Store report definition.
2. Pull source data from target services.
3. Apply filters, grouping, and summary logic.
4. Return report results without owning source truth.

## Change Rules
- Cross-service field names drive report utility; keep source schemas documented.
