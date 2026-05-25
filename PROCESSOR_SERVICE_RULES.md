# Processor Service Rules

## Overview

`processor-service` runs shared validators and operators for business submissions.

It stores each processor as a `ProcessorDefinition` with:

- `processorKey`
- `targetType`
- `validatorsJson`
- `operatorsJson`
- `description`
- `active`

## Run Contract

Request:

`POST /api/processor-service/processors/{processorKey}/run`

```json
{
  "targetType": "COMMERCE",
  "payload": {
    "documentType": "INVOICE",
    "currency": "irr",
    "subtotal": 1500000,
    "discountTotal": 100000,
    "taxTotal": 210000
  }
}
```

Response:

```json
{
  "valid": true,
  "errors": [],
  "payload": {
    "documentType": "INVOICE",
    "currency": "IRR",
    "subtotal": 1500000,
    "discountTotal": 100000,
    "taxTotal": 210000,
    "grandTotal": 1610000,
    "documentStatus": "DRAFT"
  }
}
```

## Validator Rule Shape

```json
{
  "type": "REQUIRED",
  "field": "fullName",
  "message": "fullName is required"
}
```

Fields:

- `type`
- `field`
- `message`
- `value`
- `values`
- `pattern`

Supported validator types:

- `REQUIRED`
- `MIN_LENGTH`
- `MAX_LENGTH`
- `REGEX`
- `ENUM`
- `DECIMAL_MIN`
- `DECIMAL_MAX`

## Operator Rule Shape

```json
{
  "type": "SUM_FIELDS",
  "targetField": "grandTotal",
  "sourceFields": ["subtotal", "taxTotal"]
}
```

Fields:

- `type`
- `field`
- `targetField`
- `sourceField`
- `sourceFields`
- `value`
- `separator`

Supported operator types:

- `SET_FIELD`
- `COPY_FIELD`
- `TRIM`
- `UPPERCASE`
- `LOWERCASE`
- `CONCAT_FIELDS`
- `SUM_FIELDS`
- `MULTIPLY_FIELDS`

## Example: Lead Normalizer

`targetType = CRM`

`operatorsJson`

```json
[
  {
    "type": "TRIM",
    "field": "fullName"
  },
  {
    "type": "LOWERCASE",
    "field": "email"
  },
  {
    "type": "SET_FIELD",
    "targetField": "status",
    "value": "NEW"
  }
]
```

`validatorsJson`

```json
[
  {
    "type": "REQUIRED",
    "field": "fullName",
    "message": "Lead name is required"
  },
  {
    "type": "REGEX",
    "field": "email",
    "pattern": "^[^@]+@[^@]+\\.[^@]+$",
    "message": "Email format is invalid"
  }
]
```

## Example: Invoice Defaults

`targetType = COMMERCE`

`operatorsJson`

```json
[
  {
    "type": "UPPERCASE",
    "field": "currency"
  },
  {
    "type": "SET_FIELD",
    "targetField": "documentStatus",
    "value": "DRAFT"
  },
  {
    "type": "SUM_FIELDS",
    "targetField": "grandTotal",
    "sourceFields": ["subtotal", "taxTotal"]
  }
]
```

`validatorsJson`

```json
[
  {
    "type": "REQUIRED",
    "field": "customerKey",
    "message": "customerKey is required"
  },
  {
    "type": "DECIMAL_MIN",
    "field": "subtotal",
    "value": "0",
    "message": "subtotal must be zero or positive"
  }
]
```

## Example: Content Publisher

`targetType = CONTENT`

`operatorsJson`

```json
[
  {
    "type": "TRIM",
    "field": "title"
  },
  {
    "type": "SET_FIELD",
    "targetField": "publicationStatus",
    "value": "DRAFT"
  }
]
```

`validatorsJson`

```json
[
  {
    "type": "REQUIRED",
    "field": "key",
    "message": "key is required"
  },
  {
    "type": "REQUIRED",
    "field": "title",
    "message": "title is required"
  }
]
```

## Submission Usage

Business services accept `processorKey` on create and update:

- `POST /api/content-service/content?processorKey=content-publisher`
- `POST /api/catalog-service/items?processorKey=product-defaults`
- `POST /api/crm-service/records?processorKey=lead-normalizer`
- `POST /api/commerce-service/documents?processorKey=invoice-defaults`
- `POST /api/finance-service/transactions?processorKey=payment-defaults`
- `POST /api/inventory-service/items?processorKey=stock-defaults`
