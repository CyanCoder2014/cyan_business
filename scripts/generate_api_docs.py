#!/usr/bin/env python3
import json
from collections import OrderedDict, defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
POSTMAN_DIR = ROOT / "docs" / "postman"
SWAGGER_DIR = ROOT / "docs" / "swagger"
SWAGGER_SERVICES_DIR = SWAGGER_DIR / "services"
DEFAULT_GATEWAY_BASE_URL = "http://localhost:8001"
DEFAULT_USERNAME = "cyan-admin"
DEFAULT_PASSWORD = "admin123"
DEFINITION_LIST_PATHS = {
    "/endpoint/entities/definitions",
    "/internal/entities/definitions",
}
DEFINITION_PAGE_QUERY = [
    {"name": "page", "example": "0"},
    {"name": "size", "example": "20"},
    {"name": "sort", "example": "entityKey,asc"},
]


def example_dynamic_definition():
    return {
        "entityKey": "catalog-item",
        "tenantKey": "demo-tenant",
        "siteKey": "main-site",
        "definition": {
            "entityKey": "catalog-item",
            "title": "Catalog Item",
            "fields": [
                {"key": "title", "type": "text", "required": True},
                {"key": "price", "type": "number", "required": True},
                {"key": "status", "type": "select", "options": ["DRAFT", "PUBLISHED"]},
            ],
        },
    }


def example_dynamic_record():
    return {
        "recordKey": "item-spiffy-bag",
        "tenantKey": "demo-tenant",
        "siteKey": "main-site",
        "data": {
            "title": "Spiffy Everyday Bag",
            "slug": "spiffy-everyday-bag",
            "price": 1250000,
            "currency": "IRR",
            "status": "PUBLISHED",
        },
    }


def example_payment_initiation():
    return {
        "paymentMethodKey": "zarinpal-main",
        "orderKey": "order-1001",
        "invoiceKey": "invoice-1001",
        "customerKey": "customer-farid",
        "relatedService": "checkout-service",
        "relatedEntityType": "order",
        "relatedEntityKey": "order-1001",
        "amount": 1250000,
        "currency": "IRR",
        "description": "Order payment for Spiffy checkout",
        "callbackUrl": f"{DEFAULT_GATEWAY_BASE_URL}/public/payment/callback/ZARINPAL/order-1001",
        "successUrl": "http://localhost:3000/checkout/success",
        "failureUrl": "http://localhost:3000/checkout/failure",
        "metaData": {"cartKey": "cart-1001", "source": "storefront"},
    }


def example_notification():
    return {
        "messageKey": "order-created-1001",
        "channel": "EMAIL",
        "templateKey": "order-created-email",
        "provider": "smtp-default",
        "dispatchMode": "SYNC",
        "recipient": "customer@example.com",
        "subject": "Your order is confirmed",
        "body": "Order {{orderNumber}} has been created.",
        "model": {"orderNumber": "1001", "customerName": "Farid"},
        "relatedRef": {"service": "checkout-service", "entityKey": "order", "recordKey": "order-1001"},
    }


def ep(service, method, path, summary, auth="bearer", body=None, query=None, response=None, description=None):
    return {
        "service": service,
        "method": method.upper(),
        "path": path,
        "summary": summary,
        "auth": auth,
        "body": body,
        "query": query or [],
        "response": response if response is not None else {"status": "ok"},
        "description": description or "",
    }


def build_endpoints():
    endpoints = []

    endpoints.extend([
        ep("SSO", "POST", "/api/sso/auth/login", "Login", auth="none", body={
            "clientId": "panel.sampv3",
            "username": DEFAULT_USERNAME,
            "password": DEFAULT_PASSWORD,
            "captchaChallengeId": None,
            "captchaAnswer": None,
            "otpCode": None,
            "deviceId": "chrome-local"
        }, response={
            "accessToken": "{{access_token}}",
            "refreshToken": "{{refresh_token}}",
            "tokenType": "Bearer",
            "expiresIn": 3600,
            "sessionId": "{{session_id}}"
        }),
        ep("SSO", "POST", "/api/sso/auth/logout", "Logout", auth="bearer", body={"sessionId": "{{session_id}}"}),
        ep("SSO", "POST", "/api/sso/auth/refresh", "Refresh Token", auth="none", body={
            "clientId": "panel.sampv3",
            "refreshToken": "{{refresh_token}}"
        }),
        ep("SSO", "POST", "/api/sso/auth/introspect", "Token Introspect", auth="none", body={"token": "{{access_token}}"}),
        ep("SSO", "POST", "/api/sso/auth/otp/send", "Auth OTP Send", auth="none", body={"username": DEFAULT_USERNAME, "phoneNumber": "+989121234567"}),
        ep("SSO", "POST", "/api/sso/auth/fido/challenge", "Auth FIDO Challenge", auth="none", body={"username": DEFAULT_USERNAME, "deviceId": "chrome-local"}),
        ep("SSO", "POST", "/api/sso/auth/fido/verify", "Auth FIDO Verify", auth="none", body={"username": DEFAULT_USERNAME, "challengeId": "challenge-1", "credential": "signed-payload"}),
        ep("SSO", "GET", "/.well-known/jwks.json", "JWKS", auth="none"),
        ep("SSO", "GET", "/.well-known/openid-configuration", "OpenID Configuration", auth="none"),
        ep("SSO", "POST", "/api/sso/captcha/challenges", "Create Captcha Challenge", auth="none"),
        ep("SSO", "POST", "/api/sso/captcha/verify", "Verify Captcha", auth="none", body={
            "challengeId": "{{captcha_challenge_id}}",
            "answer": "7H2K"
        }),
        ep("SSO", "POST", "/api/sso/otp/send", "OTP Send", auth="none", body={
            "username": DEFAULT_USERNAME,
            "destination": "+989121234567",
            "reason": "LOGIN"
        }),
        ep("SSO", "POST", "/api/sso/otp/verify", "OTP Verify", auth="none", body={
            "username": DEFAULT_USERNAME,
            "otpCode": "123456"
        }),
        ep("SSO", "POST", "/api/sso/sessions", "Create Session", auth="none", body={
            "username": DEFAULT_USERNAME,
            "clientId": "panel.sampv3",
            "deviceId": "chrome-local"
        }),
        ep("SSO", "GET", "/api/sso/sessions/{sessionId}", "Get Session", auth="bearer"),
        ep("SSO", "POST", "/api/sso/sessions/revoke", "Revoke Session", auth="bearer", body={"sessionId": "{{session_id}}"}),
        ep("SSO", "POST", "/api/sso/users", "Register User", auth="none", body={
            "username": DEFAULT_USERNAME,
            "password": DEFAULT_PASSWORD,
            "email": "farid@example.com",
            "phoneNumber": "+989121234567",
            "mfaEnabled": False,
            "roles": ["ROLE_USER", "ROLE_ADMIN"]
        }),
        ep("SSO", "GET", "/api/sso/users/{username}", "Get User", auth="bearer"),
        ep("SSO", "POST", "/api/sso/users/verify-password", "Verify Password", auth="none", body={
            "username": DEFAULT_USERNAME,
            "password": DEFAULT_PASSWORD
        }),
        ep("SSO", "POST", "/api/sso/fido/challenge", "FIDO Challenge", auth="none", body={"username": DEFAULT_USERNAME, "deviceId": "chrome-local"}),
        ep("SSO", "POST", "/api/sso/fido/verify", "FIDO Verify", auth="none", body={"username": DEFAULT_USERNAME, "challengeId": "challenge-1", "credential": "signed-payload"}),
    ])

    endpoints.extend([
        ep("AI Orchestrator", "POST", "/endpoint/ai-orchestrator/generate/app", "Generate Platform App", body={
            "prompt": "Want an e-commerce site like Spiffy with CRM, notifications, and Zarinpal payment.",
            "appType": "ECOMMERCE",
            "tenantKey": "{{tenant_key}}",
            "siteKey": "{{site_key}}",
            "clientKey": "{{client_key}}",
            "sessionId": "{{ai_session_id}}",
            "execute": False,
            "answers": {"subdomain": "spiffy-demo"}
        }),
        ep("AI Orchestrator", "GET", "/endpoint/ai-orchestrator/blueprints", "List Blueprints"),
        ep("AI Orchestrator", "GET", "/endpoint/ai-orchestrator/blueprints/{blueprintKey}", "Get Blueprint"),
        ep("AI Orchestrator", "POST", "/endpoint/ai-orchestrator/drafts", "Create Draft", body={
            "appType": "ECOMMERCE",
            "blueprintKey": "ecommerce-crm-zarinpal-v1",
            "tenantKey": "{{tenant_key}}",
            "siteKey": "{{site_key}}",
            "clientKey": "{{client_key}}",
            "title": "Spiffy Clone",
            "prompt": "Spiffy-like ecommerce with CRM and notifications.",
            "answers": {"subdomain": "spiffy-demo", "brandName": "Spiffy Demo"}
        }),
        ep("AI Orchestrator", "GET", "/endpoint/ai-orchestrator/drafts", "List Drafts"),
        ep("AI Orchestrator", "GET", "/endpoint/ai-orchestrator/drafts/{draftId}", "Get Draft"),
        ep("AI Orchestrator", "PATCH", "/endpoint/ai-orchestrator/drafts/{draftId}", "Patch Draft", body={
            "prompt": "Spiffy-like ecommerce with CRM, notifications, and Zarinpal.",
            "title": "Spiffy Demo Updated",
            "answersPatch": {"subdomain": "spiffy-shop", "primaryCurrency": "IRR"}
        }),
        ep("AI Orchestrator", "POST", "/endpoint/ai-orchestrator/drafts/{draftId}/provision", "Provision Draft", body={
            "mode": "EXECUTE",
            "idempotencyKey": "prov-{{draft_id}}",
            "triggerType": "MANUAL",
            "triggeredBy": "postman"
        }),
        ep("AI Orchestrator", "GET", "/endpoint/ai-orchestrator/drafts/{draftId}/runs", "List Provisioning Runs"),
        ep("AI Orchestrator", "GET", "/endpoint/ai-orchestrator/runs/{runId}", "Get Provisioning Run"),
        ep("AI Orchestrator", "POST", "/endpoint/ai-orchestrator/sessions", "Create Conversation Session", body={
            "channelType": "CHAT",
            "tenantKey": "{{tenant_key}}",
            "siteKey": "{{site_key}}",
            "clientKey": "{{client_key}}",
            "draftId": "{{draft_id}}",
            "appTypeHint": "ECOMMERCE",
            "title": "Spiffy Intake",
            "extractedAnswers": {"subdomain": "spiffy-demo"}
        }),
        ep("AI Orchestrator", "GET", "/endpoint/ai-orchestrator/sessions/{sessionId}", "Get Conversation Session"),
        ep("AI Orchestrator", "POST", "/endpoint/ai-orchestrator/sessions/{sessionId}/message", "Send Conversation Message", body={
            "role": "USER",
            "content": "Use a subdomain and add CRM plus notification flows.",
            "answersPatch": {"subdomain": "spiffy-demo", "crmEnabled": True}
        }),
        ep("AI Orchestrator Internal", "POST", "/internal/ai-orchestrator/generate/app", "Generate Platform App Internal", auth="basic", body={
            "prompt": "Generate a tenant blueprint draft for a store.",
            "appType": "ECOMMERCE",
            "tenantKey": "{{tenant_key}}",
            "siteKey": "{{site_key}}",
            "clientKey": "{{client_key}}",
            "execute": False,
            "answers": {"subdomain": "internal-spiffy"}
        }),
        ep("AI Orchestrator Internal", "GET", "/internal/ai-orchestrator/blueprints", "List Blueprints Internal", auth="basic"),
        ep("AI Orchestrator Internal", "GET", "/internal/ai-orchestrator/blueprints/{blueprintKey}", "Get Blueprint Internal", auth="basic"),
        ep("AI Orchestrator Internal", "POST", "/internal/ai-orchestrator/drafts", "Create Draft Internal", auth="basic", body={
            "appType": "ECOMMERCE",
            "blueprintKey": "ecommerce-crm-zarinpal-v1",
            "tenantKey": "{{tenant_key}}",
            "siteKey": "{{site_key}}",
            "clientKey": "{{client_key}}",
            "title": "Internal Spiffy Draft",
            "prompt": "Internal provisioning path.",
            "answers": {"subdomain": "internal-spiffy"}
        }),
        ep("AI Orchestrator Internal", "GET", "/internal/ai-orchestrator/drafts", "List Drafts Internal", auth="basic"),
        ep("AI Orchestrator Internal", "GET", "/internal/ai-orchestrator/drafts/{draftId}", "Get Draft Internal", auth="basic"),
        ep("AI Orchestrator Internal", "PATCH", "/internal/ai-orchestrator/drafts/{draftId}", "Patch Draft Internal", auth="basic", body={"title": "Internal Updated"}),
        ep("AI Orchestrator Internal", "POST", "/internal/ai-orchestrator/drafts/{draftId}/provision", "Provision Draft Internal", auth="basic", body={"mode": "EXECUTE"}),
        ep("AI Orchestrator Internal", "GET", "/internal/ai-orchestrator/drafts/{draftId}/runs", "List Runs Internal", auth="basic"),
        ep("AI Orchestrator Internal", "GET", "/internal/ai-orchestrator/runs/{runId}", "Get Run Internal", auth="basic"),
        ep("AI Orchestrator Internal", "POST", "/internal/ai-orchestrator/sessions", "Create Session Internal", auth="basic", body={
            "channelType": "SYSTEM",
            "tenantKey": "{{tenant_key}}",
            "siteKey": "{{site_key}}",
            "clientKey": "{{client_key}}",
            "title": "Internal Session"
        }),
        ep("AI Orchestrator Internal", "GET", "/internal/ai-orchestrator/sessions/{sessionId}", "Get Session Internal", auth="basic"),
        ep("AI Orchestrator Internal", "POST", "/internal/ai-orchestrator/sessions/{sessionId}/message", "Message Session Internal", auth="basic", body={
            "role": "SYSTEM",
            "content": "Continue blueprint enrichment."
        }),
    ])

    for prefix, service, auth in [
        ("/endpoint/entities", "Dynamic Entity", "bearer"),
        ("/internal/entities", "Dynamic Entity Internal", "basic"),
    ]:
        endpoints.extend([
            ep(service, "POST", f"{prefix}/definitions", "Create Definition", auth=auth, body=example_dynamic_definition()),
            ep(service, "PUT", f"{prefix}/definitions/{{entityKey}}", "Update Definition", auth=auth, body=example_dynamic_definition()),
            ep(
                service,
                "GET",
                f"{prefix}/definitions",
                "List Definitions",
                auth=auth,
                query=DEFINITION_PAGE_QUERY,
                description=(
                    (
                        "Paginated internal tenant/site-scoped entity definitions "
                        "using Basic authentication. Default page=0, size=20; "
                        "maximum size=200."
                    )
                    if auth == "basic"
                    else (
                        "Paginated tenant/site-scoped entity definitions. Default "
                        "page=0, size=20; maximum size=200. Supported sort fields: "
                        "entityKey, title, entityType, createdAt, updatedAt."
                    )
                ),
            ),
            ep(service, "GET", f"{prefix}/definitions/{{entityKey}}", "Get Definition", auth=auth),
            ep(service, "DELETE", f"{prefix}/definitions/{{entityKey}}", "Delete Definition", auth=auth),
            ep(service, "GET", f"{prefix}/templates", "List Templates", auth=auth),
            ep(service, "GET", f"{prefix}/templates/{{templateKey}}", "Get Template", auth=auth),
            ep(service, "POST", f"{prefix}/templates/{{templateKey}}/definitions", "Create Definition From Template", auth=auth, body={
                "entityKey": "spiffy-order",
                "tenantKey": "{{tenant_key}}",
                "siteKey": "{{site_key}}"
            }),
            ep(service, "POST", f"{prefix}/records/{{entityKey}}/validate", "Validate Record", auth=auth, body=example_dynamic_record()["data"]),
            ep(service, "POST", f"{prefix}/submit/{{entityKey}}", "Submit Map Record", auth=auth, body=example_dynamic_record()["data"], query=[{"name": "recordKey", "example": "item-spiffy-bag"}]),
            ep(service, "POST", f"{prefix}/records/{{entityKey}}", "Create Record", auth=auth, body=example_dynamic_record()),
            ep(service, "PUT", f"{prefix}/records/{{entityKey}}/{{recordKey}}", "Replace Record", auth=auth, body=example_dynamic_record()),
            ep(service, "PATCH", f"{prefix}/records/{{entityKey}}/{{recordKey}}", "Patch Record", auth=auth, body={"data": {"price": 1350000, "status": "PUBLISHED"}}),
            ep(service, "GET", f"{prefix}/records/{{entityKey}}", "List Records", auth=auth),
            ep(service, "GET", f"{prefix}/records/{{entityKey}}/{{recordKey}}", "Get Record", auth=auth),
            ep(service, "DELETE", f"{prefix}/records/{{entityKey}}/{{recordKey}}", "Delete Record", auth=auth),
        ])

    endpoints.extend([
        ep("BPM", "GET", "/endpoint/bpm/metadata/actions", "List BPM Action Metadata"),
        ep("BPM", "GET", "/endpoint/bpm/metadata/transition-conditions", "List BPM Transition Conditions"),
        ep("BPM", "GET", "/endpoint/bpm/flows", "List Flows"),
        ep("BPM", "GET", "/endpoint/bpm/flows/{flowKey}", "Get Flow"),
        ep("BPM", "POST", "/endpoint/bpm/flows", "Create Flow", body={
            "flowKey": "customer-onboarding",
            "version": 1,
            "active": True,
            "states": [
                {"key": "DRAFT", "label": "Draft", "automatic": False, "waitForAutomation": False},
                {"key": "REVIEW", "label": "Review", "automatic": False, "waitForAutomation": True},
            ],
            "actions": [
                {
                    "type": "RUN_AUTOMATION_BLOCK",
                    "params": {
                        "blockKey": "credit-check",
                        "automationFlowKey": "credit-screening",
                        "executionMode": "ASYNC",
                        "failurePolicy": "MARK_FAILED",
                        "body": {"customerKey": "{{record_key}}"}
                    }
                }
            ]
        }),
        ep("BPM", "POST", "/endpoint/bpm/flows/{flowKey}/activate/{version}", "Activate Flow"),
        ep("BPM", "GET", "/endpoint/bpm/managed-objects", "List Managed Objects"),
        ep("BPM", "GET", "/endpoint/bpm/managed-objects/assigned-to-me", "Assigned To Me"),
        ep("BPM", "GET", "/endpoint/bpm/managed-objects/visible-to-me", "Visible To Me"),
        ep("BPM", "POST", "/endpoint/bpm/managed-objects", "Create Managed Object", body={
            "flowKey": "customer-onboarding",
            "objectType": "crm-lead",
            "objectRef": {"entityKey": "crm-record", "recordKey": "lead-1001"},
            "payload": {"customerName": "Farid", "phone": "+989121234567", "plan": "PREMIUM"}
        }),
        ep("BPM", "POST", "/endpoint/bpm/managed-objects/{objectId}/transitions", "Execute Transition", body={
            "nextState": "REVIEW",
            "context": {"reason": "manual-review"}
        }),
        ep("BPM", "GET", "/endpoint/bpm/managed-objects/{objectId}/transitions", "List Available Transitions"),
        ep("BPM", "GET", "/endpoint/bpm/managed-objects/{objectId}/active-form", "Get Active Form"),
        ep("BPM", "POST", "/endpoint/bpm/managed-objects/{objectId}/active-form/submissions", "Submit Active Form", body={
            "formData": {"nationalCode": "0012345678", "acceptTerms": True},
            "nextState": "REVIEW",
            "context": {"submittedBy": "farid"}
        }),
        ep("BPM", "GET", "/endpoint/bpm/managed-objects/{objectId}", "Get Managed Object"),
        ep("BPM", "POST", "/public/bpm/async-actions/callbacks/{correlationKey}", "Async Callback", auth="none", body={
            "status": "SUCCEEDED",
            "output": {"screeningScore": 87, "riskBucket": "LOW"},
            "errorCode": None,
            "errorMessage": None
        }),
    ])

    for prefix, service, auth in [
        ("/internal/bpm/metadata", "BPM Internal Metadata", "basic"),
        ("/internal/bpm/flows", "BPM Internal Flow", "basic"),
        ("/internal/bpm/managed-objects", "BPM Internal Managed Objects", "basic"),
    ]:
        if prefix.endswith("/metadata"):
            endpoints.extend([
                ep(service, "GET", f"{prefix}/actions", "List BPM Action Metadata Internal", auth=auth),
                ep(service, "GET", f"{prefix}/transition-conditions", "List BPM Transition Conditions Internal", auth=auth),
            ])
        elif prefix.endswith("/flows"):
            endpoints.extend([
                ep(service, "GET", f"{prefix}", "List Flows Internal", auth=auth),
                ep(service, "GET", f"{prefix}/{{flowKey}}", "Get Flow Internal", auth=auth),
                ep(service, "POST", f"{prefix}", "Create Flow Internal", auth=auth, body={"flowKey": "internal-flow", "version": 1}),
                ep(service, "POST", f"{prefix}/{{flowKey}}/activate/{{version}}", "Activate Flow Internal", auth=auth),
            ])
        else:
            endpoints.extend([
                ep(service, "GET", f"{prefix}", "List Managed Objects Internal", auth=auth),
                ep(service, "POST", f"{prefix}", "Create Managed Object Internal", auth=auth, body={
                    "flowKey": "customer-onboarding",
                    "objectType": "crm-lead",
                    "objectRef": {"entityKey": "crm-record", "recordKey": "lead-1001"},
                    "payload": {"customerName": "Farid"}
                }),
                ep(service, "POST", f"{prefix}/{{objectId}}/transitions", "Execute Transition Internal", auth=auth, body={"nextState": "APPROVED", "context": {"source": "system"}}),
                ep(service, "GET", f"{prefix}/{{objectId}}/transitions", "List Available Transitions Internal", auth=auth),
                ep(service, "GET", f"{prefix}/{{objectId}}/active-form", "Get Active Form Internal", auth=auth),
                ep(service, "POST", f"{prefix}/{{objectId}}/active-form/submissions", "Submit Active Form Internal", auth=auth, body={"formData": {"approved": True}}),
                ep(service, "GET", f"{prefix}/{{objectId}}", "Get Managed Object Internal", auth=auth),
            ])

    endpoints.extend([
        ep("Automation Orchestrator", "POST", "/internal/automation-orchestrator/executions/start", "Start Automation Execution", auth="basic", body={
            "blockKey": "credit-check",
            "automationFlowKey": "credit-screening",
            "executionMode": "ASYNC",
            "failurePolicy": "MARK_FAILED",
            "correlationKey": "corr-1001",
            "callbackPath": "/public/bpm/async-actions/callbacks/corr-1001",
            "tenantKey": "{{tenant_key}}",
            "siteKey": "{{site_key}}",
            "input": {"customerKey": "lead-1001"},
            "context": {"objectId": "{{object_id}}"},
            "inlineFragment": {"type": "MAP_OUTPUT", "mappings": {"riskBucket": "$.riskBucket"}},
            "maxRetries": 2,
            "timeoutSeconds": 120,
            "delayMillis": 0
        }),
        ep("Automation Orchestrator", "GET", "/internal/automation-orchestrator/executions/{executionId}", "Get Automation Execution", auth="basic"),
        ep("Automation Orchestrator", "POST", "/internal/automation-orchestrator/executions/{executionId}/cancel", "Cancel Automation Execution", auth="basic"),
    ])

    endpoints.extend([
        ep("Checkout", "GET", "/internal/checkout/sessions/{entityKey}/{recordKey}/snapshot", "Get Checkout Snapshot", auth="basic"),
        ep("Checkout", "POST", "/internal/checkout/sessions/{entityKey}/{recordKey}/initiate-payment", "Initiate Checkout Payment", auth="basic", body={
            "paymentMethodKey": "zarinpal-main",
            "orderKey": "order-1001",
            "invoiceKey": "invoice-1001",
            "customerKey": "customer-farid",
            "relatedService": "checkout-service",
            "relatedEntityType": "order",
            "relatedEntityKey": "order-1001",
            "amount": 1250000,
            "currency": "IRR",
            "description": "Checkout payment",
            "callbackUrl": f"{DEFAULT_GATEWAY_BASE_URL}/public/payment/callback/ZARINPAL/order-1001",
            "successUrl": "http://localhost:3000/checkout/success",
            "failureUrl": "http://localhost:3000/checkout/failure",
            "metaData": {"cartKey": "cart-1001"}
        }),
        ep("Checkout", "POST", "/internal/checkout/sessions/{entityKey}/{recordKey}/verify-payment", "Verify Checkout Payment", auth="basic", body={
            "transactionKey": "{{transaction_key}}",
            "payload": {"authority": "A0000001", "status": "OK"}
        }),
        ep("Checkout", "POST", "/internal/checkout/sessions/{entityKey}/{recordKey}/advance", "Advance Checkout Lifecycle", auth="basic", body={
            "status": "COMPLETED",
            "orderStatus": "CONFIRMED",
            "paymentStatus": "PAID",
            "fulfillmentStatus": "PENDING",
            "sendNotifications": True,
            "eventCode": "ORDER_CONFIRMED"
        }),
    ])

    endpoints.extend([
        ep("Payment Orchestrator", "GET", "/internal/payment-orchestrator/methods", "List Available Payment Methods", auth="basic"),
        ep("Payment Orchestrator", "POST", "/internal/payment-orchestrator/sessions/initiate", "Initiate Payment Session", auth="basic", body=example_payment_initiation()),
        ep("Payment Orchestrator", "POST", "/internal/payment-orchestrator/transactions/{transactionKey}/verify", "Verify Orchestrated Payment", auth="basic", body={"payload": {"authority": "A0000001", "status": "OK"}}),
    ])

    endpoints.extend([
        ep("Payment", "GET", "/endpoint/payment/methods", "List Payment Methods"),
        ep("Payment", "GET", "/endpoint/payment/admin/methods", "List Admin Payment Methods"),
        ep("Payment", "GET", "/endpoint/payment/admin/methods/{methodKey}", "Get Admin Payment Method"),
        ep("Payment", "POST", "/endpoint/payment/admin/methods", "Create Payment Method", body={
            "methodKey": "zarinpal-main",
            "displayName": "Zarinpal Main",
            "providerCode": "ZARINPAL",
            "region": "IRANIAN",
            "flowType": "REDIRECT",
            "enabled": True,
            "active": True,
            "priorityOrder": 1,
            "supportedCurrencies": ["IRR"],
            "configuration": {"merchantId": "merchant-demo", "callbackBaseUrl": DEFAULT_GATEWAY_BASE_URL},
            "description": "Primary Zarinpal payment method"
        }),
        ep("Payment", "PUT", "/endpoint/payment/admin/methods/{methodKey}", "Update Payment Method", body={
            "methodKey": "zarinpal-main",
            "displayName": "Zarinpal Main Updated",
            "providerCode": "ZARINPAL",
            "region": "IRANIAN",
            "flowType": "REDIRECT",
            "enabled": True,
            "active": True,
            "priorityOrder": 1,
            "supportedCurrencies": ["IRR"],
            "configuration": {"merchantId": "merchant-demo", "callbackBaseUrl": DEFAULT_GATEWAY_BASE_URL},
            "description": "Updated method"
        }),
        ep("Payment", "DELETE", "/endpoint/payment/admin/methods/{methodKey}", "Delete Payment Method"),
        ep("Payment", "POST", "/endpoint/payment/transactions/initiate", "Initiate Payment", body=example_payment_initiation()),
        ep("Payment", "POST", "/endpoint/payment/transactions/{transactionKey}/verify", "Verify Payment", body={"payload": {"authority": "A0000001", "status": "OK"}}),
        ep("Payment", "GET", "/endpoint/payment/transactions/{transactionKey}", "Get Payment Transaction"),
        ep("Payment", "GET", "/endpoint/payment/transactions", "List Payment Transactions"),
        ep("Payment Internal", "GET", "/internal/payment/methods", "List Payment Methods Internal", auth="basic"),
        ep("Payment Internal", "GET", "/internal/payment/admin/methods", "List Admin Payment Methods Internal", auth="basic"),
        ep("Payment Internal", "GET", "/internal/payment/admin/methods/{methodKey}", "Get Admin Payment Method Internal", auth="basic"),
        ep("Payment Internal", "POST", "/internal/payment/admin/methods", "Create Payment Method Internal", auth="basic", body={
            "methodKey": "zarinpal-main",
            "displayName": "Zarinpal Main",
            "providerCode": "ZARINPAL",
            "region": "IRANIAN",
            "flowType": "REDIRECT",
            "enabled": True,
            "active": True,
            "priorityOrder": 1,
            "supportedCurrencies": ["IRR"],
            "configuration": {"merchantId": "merchant-demo"},
            "description": "Internal create"
        }),
        ep("Payment Internal", "PUT", "/internal/payment/admin/methods/{methodKey}", "Update Payment Method Internal", auth="basic", body={
            "methodKey": "zarinpal-main",
            "displayName": "Zarinpal Main",
            "providerCode": "ZARINPAL",
            "region": "IRANIAN",
            "flowType": "REDIRECT",
            "enabled": True,
            "active": True,
            "priorityOrder": 2,
            "supportedCurrencies": ["IRR"],
            "configuration": {"merchantId": "merchant-demo"},
            "description": "Internal update"
        }),
        ep("Payment Internal", "POST", "/internal/payment/transactions/initiate", "Initiate Payment Internal", auth="basic", body=example_payment_initiation()),
        ep("Payment Internal", "POST", "/internal/payment/transactions/{transactionKey}/verify", "Verify Payment Internal", auth="basic", body={"payload": {"authority": "A0000001"}}),
        ep("Payment Internal", "GET", "/internal/payment/transactions/{transactionKey}", "Get Payment Transaction Internal", auth="basic"),
        ep("Payment Internal", "GET", "/internal/payment/transactions", "List Payment Transactions Internal", auth="basic"),
        ep("Payment Public", "GET", "/public/payment/callback/{providerCode}/{transactionKey}", "Payment Callback GET", auth="none", query=[{"name": "Authority", "example": "A0000001"}, {"name": "Status", "example": "OK"}]),
        ep("Payment Public", "POST", "/public/payment/callback/{providerCode}/{transactionKey}", "Payment Callback POST", auth="none", body={"Authority": "A0000001", "Status": "OK"}),
        ep("Payment Public", "GET", "/public/payment/simulate/{providerCode}/{transactionKey}", "Simulate Payment Callback", auth="none", query=[{"name": "status", "example": "SUCCESS"}]),
    ])

    for prefix, service, auth in [
        ("/endpoint/notifications", "Notification", "bearer"),
        ("/internal/notifications", "Notification Internal", "basic"),
    ]:
        endpoints.extend([
            ep(service, "POST", f"{prefix}/send", "Send Notification", auth=auth, body=example_notification()),
            ep(service, "POST", f"{prefix}/send-async", "Send Notification Async", auth=auth, body={**example_notification(), "dispatchMode": "ASYNC"}),
            ep(service, "GET", f"{prefix}/messages/{{messageKey}}", "Get Notification Message", auth=auth),
        ])

    endpoints.extend([
        ep("Storefront", "GET", "/public/storefront/resolve", "Resolve Route", auth="none", query=[{"name": "path", "example": "/products/spiffy-everyday-bag"}]),
        ep("Storefront", "GET", "/public/storefront/render", "Render Route", auth="none", query=[{"name": "path", "example": "/products/spiffy-everyday-bag"}]),
        ep("Storefront", "GET", "/public/storefront/page", "Render HTML Page", auth="none", query=[{"name": "path", "example": "/products/spiffy-everyday-bag"}], response="<html>...</html>"),
        ep("Storefront", "GET", "/public/storefront/sitemap", "Get Sitemap", auth="none"),
        ep("Storefront", "GET", "/public/storefront/sitemap.xml", "Get Sitemap XML", auth="none", response="<urlset/>"),
        ep("Storefront", "GET", "/public/storefront/robots.txt", "Get Robots", auth="none", response="User-agent: *"),
    ])

    endpoints.extend([
        ep("Media", "POST", "/internal/media/assets/prepare-upload", "Prepare Upload", auth="basic", body={
            "assetKey": "hero-banner-1",
            "assetType": "IMAGE",
            "originalFileName": "banner.jpg",
            "mimeType": "image/jpeg",
            "visibility": "PUBLIC",
            "altText": "Spiffy banner",
            "caption": "Homepage banner",
            "title": "Spiffy Hero",
            "license": "owned",
            "bucket": "media",
            "path": "homepage/banner.jpg",
            "width": 1600,
            "height": 900,
            "sizeBytes": 245000
        }),
        ep("Media", "GET", "/internal/media/assets/{assetKey}", "Get Media Asset Internal", auth="basic"),
        ep("Media", "GET", "/public/media/assets/{assetKey}", "Get Media Asset", auth="none"),
        ep("Media", "GET", "/public/media/assets/{assetKey}/variants/{variantKey}", "Get Media Asset Variant", auth="none"),
    ])

    endpoints.extend([
        ep("Search", "GET", "/public/search-index/search", "Search", auth="none", query=[
            {"name": "q", "example": "spiffy bag"},
            {"name": "entityTypes", "example": "product,content"},
            {"name": "filterKey", "example": "status"},
            {"name": "filterValue", "example": "PUBLISHED"},
            {"name": "sort", "example": "title_asc"},
            {"name": "page", "example": "0"},
            {"name": "size", "example": "20"},
        ]),
        ep("Search", "GET", "/public/search-index/suggest", "Suggest", auth="none", query=[
            {"name": "q", "example": "spi"},
            {"name": "limit", "example": "8"},
        ]),
        ep("Search Internal", "GET", "/internal/search-index/search", "Search Internal", auth="basic", query=[
            {"name": "q", "example": "crm customer"},
            {"name": "entityTypes", "example": "crm-record"},
        ]),
        ep("Search Internal", "POST", "/internal/search-index/sync/{sourceServiceKey}/{sourceEntityKey}", "Sync Search Index", auth="basic"),
    ])

    crud_services = [
        ("Content", "/api/content-service/content", "content", "contentKey"),
        ("Catalog", "/api/catalog-service/items", "item", "itemKey"),
        ("CRM", "/api/crm-service/records", "record", "recordKey"),
        ("Commerce", "/api/commerce-service/documents", "document", "documentKey"),
        ("Finance", "/api/finance-service/transactions", "transaction", "transactionKey"),
        ("Inventory", "/api/inventory-service/items", "item", "itemKey"),
    ]
    for service, base, noun, key in crud_services:
        sample = {
            key: f"{noun}-1001",
            "title": f"Sample {service} {noun.title()}",
            "status": "ACTIVE",
            "payload": {"source": "postman", "tenantKey": "{{tenant_key}}", "siteKey": "{{site_key}}"}
        }
        endpoints.extend([
            ep(service, "POST", base, f"Create {service} {noun.title()}", body=sample),
            ep(service, "GET", base, f"List {service} {noun.title()}s"),
            ep(service, "GET", f"{base}/{{{key}}}", f"Get {service} {noun.title()}"),
            ep(service, "PUT", f"{base}/{{{key}}}", f"Update {service} {noun.title()}", body=sample),
            ep(service, "DELETE", f"{base}/{{{key}}}", f"Delete {service} {noun.title()}"),
            ep(service, "GET", f"{base}/internal/export", f"Export {service}", auth="bearer"),
        ])
    endpoints.append(ep("Content", "GET", "/api/content-service/content/slug/{slug}", "Get Content By Slug"))

    endpoints.extend([
        ep("Report", "POST", "/api/report-service/reports", "Create Report", body={
            "reportKey": "sales-summary",
            "title": "Sales Summary",
            "status": "ACTIVE",
            "definition": {"type": "TABLE", "entityKey": "order"}
        }),
        ep("Report", "GET", "/api/report-service/reports", "List Reports"),
        ep("Report", "GET", "/api/report-service/reports/{reportKey}", "Get Report"),
        ep("Report", "PUT", "/api/report-service/reports/{reportKey}", "Update Report", body={
            "reportKey": "sales-summary",
            "title": "Sales Summary Updated",
            "status": "ACTIVE",
            "definition": {"type": "TABLE", "entityKey": "order"}
        }),
        ep("Report", "DELETE", "/api/report-service/reports/{reportKey}", "Delete Report"),
        ep("Report", "POST", "/api/report-service/reports/{reportKey}/run", "Run Report", body={"filters": {"status": "PAID"}}),
        ep("Report Internal", "POST", "/internal/dynamic-reports/{entityKey}/{recordKey}/run", "Run Dynamic Report Internal", auth="basic", body={"triggeredBy": "system"}),
    ])

    endpoints.extend([
        ep("Processor", "POST", "/api/processor-service/processors", "Create Processor", body={
            "processorKey": "sync-customer-profile",
            "title": "Sync Customer Profile",
            "status": "ACTIVE",
            "config": {"targetService": "crm-service"}
        }),
        ep("Processor", "GET", "/api/processor-service/processors", "List Processors"),
        ep("Processor", "GET", "/api/processor-service/processors/{processorKey}", "Get Processor"),
        ep("Processor", "PUT", "/api/processor-service/processors/{processorKey}", "Update Processor", body={
            "processorKey": "sync-customer-profile",
            "title": "Sync Customer Profile Updated",
            "status": "ACTIVE",
            "config": {"targetService": "crm-service"}
        }),
        ep("Processor", "DELETE", "/api/processor-service/processors/{processorKey}", "Delete Processor"),
        ep("Processor", "POST", "/api/processor-service/processors/{processorKey}/run", "Run Processor", body={"input": {"recordKey": "customer-1001"}}),
    ])

    endpoints.extend([
        ep("Event", "POST", "/api/event-service/events", "Publish Event", body={
            "eventKey": "order-created-1001",
            "eventType": "ORDER_CREATED",
            "payload": {"orderKey": "order-1001", "amount": 1250000}
        }),
        ep("Event", "GET", "/api/event-service/events", "List Events"),
        ep("Event", "GET", "/api/event-service/events/{eventKey}", "Get Event"),
    ])

    for service, path in [
        ("CRM Automation", "/api/crm-automation-service/actions"),
        ("Finance Automation", "/api/finance-automation-service/actions"),
        ("Inventory Automation", "/api/inventory-automation-service/actions"),
        ("Report Automation", "/api/report-automation-service/records"),
    ]:
        endpoints.append(ep(service, "GET", path, f"List {service}"))

    endpoints.append(ep("Pricing Promotion", "POST", "/internal/pricing-promotions/evaluate", "Evaluate Pricing Promotion", auth="basic", body={
        "items": [{"sku": "spiffy-bag", "quantity": 1, "unitPrice": 1250000}],
        "customerKey": "customer-farid",
        "couponCode": "NEW10"
    }))

    endpoints.extend([
        ep("Legacy Buyer", "GET", "/v2/api/buyer", "Buyer Home", auth="none"),
        ep("Legacy Buyer", "GET", "/v2/api/buyer-service/buyers", "List Buyers", auth="none"),
        ep("Legacy Buyer", "POST", "/v2/api/buyer-service/buyers", "Create Buyer", auth="none", body={"buyerCode": "buyer-1001", "name": "Farid Buyer"}),
        ep("Legacy Client", "GET", "/v2/api/client-service/clients", "List Clients", auth="none"),
        ep("Legacy Client", "POST", "/v2/api/client-service/clients", "Create Client", auth="none", body={"clientCode": "client-1001", "name": "Spiffy Client"}),
        ep("Legacy Client", "GET", "/v2/api/client-service/clients1", "List Clients Legacy 1", auth="none"),
        ep("Legacy Client", "GET", "/v2/api/client-service/companies", "List Companies", auth="none"),
        ep("Legacy Client", "GET", "/v2/api/client-service/companies/", "List Companies Slash", auth="none"),
        ep("Legacy Client", "POST", "/v2/api/client-service/companies", "Create Company", auth="none", body={"companyCode": "comp-1001", "name": "Spiffy Co"}),
        ep("Legacy Factor", "GET", "/v2/api/factor-service/factors", "List Factors", auth="none"),
        ep("Legacy Factor", "POST", "/v2/api/factor-service/factors", "Create Factor", auth="none", body={"factorCode": "factor-1001", "amount": 1250000}),
        ep("Legacy Product", "GET", "/v2/api/product", "Product Home", auth="none"),
        ep("Legacy Product", "GET", "/v2/api/product-service/products", "List Products", auth="none"),
        ep("Legacy Product", "POST", "/v2/api/product-service/products", "Create Product", auth="none", body={"productCode": "prod-1001", "name": "Spiffy Bag"}),
    ])

    endpoints.extend([
        ep("Tax Pay Sys", "GET", "/v2/api/tax-service", "Tax Service Home", auth="none"),
        ep("Tax Pay Sys", "GET", "/v2/api/tax-service/test", "Tax Service Test", auth="none"),
        ep("Tax Pay Sys", "GET", "/v2/api/tax-service/sign", "Tax Service Sign", auth="none", query=[{"name": "data", "example": "hello"}]),
        ep("Tax Pay Sys", "GET", "/v2/api/tax-service/encrypt", "Tax Service Encrypt", auth="none", query=[{"name": "data", "example": "hello"}]),
        ep("Tax Pay Sys", "GET", "/v2/api/tax-service/get-factors", "Tax Service Get Factors", auth="none"),
        ep("Tax Pay Sys", "GET", "/v2/api/tax/auth/set-token", "Tax Auth Set Token", auth="none"),
        ep("Tax Pay Sys", "POST", "/v2/api/tax/invoice/send-invoice", "Tax Send Invoice", auth="none", body={"invoiceNumber": "1001", "amount": 1250000}),
        ep("Tax Pay Sys", "GET", "/v2/api/tax/inquiry/get-by-uid", "Tax Inquiry By UID", auth="none", query=[{"name": "uid", "example": "uid-1001"}]),
        ep("Tax Pay Sys", "GET", "/v2/api/tax/inquiry/get-by-reference", "Tax Inquiry By Reference", auth="none", query=[{"name": "referenceNumber", "example": "ref-1001"}]),
        ep("Tax Pay Sys", "GET", "/v2/api/tax/server-info/get-info", "Tax Server Info", auth="none"),
        ep("Tax Pay Sys", "GET", "/v2/api/tax-service/person-data/get-info", "Tax Person Data", auth="none", query=[{"name": "nationalId", "example": "0012345678"}]),
    ])

    return endpoints


def auth_for_endpoint(auth_type):
    if auth_type == "bearer":
        return {
            "type": "bearer",
            "bearer": [{"key": "token", "value": "{{access_token}}", "type": "string"}],
        }
    if auth_type == "basic":
        return {
            "type": "basic",
            "basic": [
                {"key": "username", "value": "{{internal_basic_username}}", "type": "string"},
                {"key": "password", "value": "{{internal_basic_password}}", "type": "string"},
            ],
        }
    return {"type": "noauth"}


def example_from_path_param(name):
    examples = {
        "sessionId": "{{session_id}}",
        "blueprintKey": "ecommerce-crm-zarinpal-v1",
        "draftId": "{{draft_id}}",
        "runId": "{{run_id}}",
        "entityKey": "catalog-item",
        "recordKey": "{{record_key}}",
        "objectId": "{{object_id}}",
        "flowKey": "customer-onboarding",
        "version": "1",
        "executionId": "{{execution_id}}",
        "transactionKey": "{{transaction_key}}",
        "providerCode": "ZARINPAL",
        "methodKey": "zarinpal-main",
        "messageKey": "{{message_key}}",
        "assetKey": "hero-banner-1",
        "variantKey": "thumb",
        "sourceServiceKey": "catalog-service",
        "sourceEntityKey": "catalog-item",
        "contentKey": "content-1001",
        "itemKey": "item-1001",
        "documentKey": "document-1001",
        "transactionKey": "{{transaction_key}}",
        "reportKey": "sales-summary",
        "processorKey": "sync-customer-profile",
        "eventKey": "order-created-1001",
        "username": DEFAULT_USERNAME,
        "slug": "homepage",
        "correlationKey": "corr-1001",
    }
    return examples.get(name, f"{{{{{name}}}}}")


def path_to_postman_url(path, query):
    raw = "{{gateway_base_url}}" + path
    for part in set(p for p in path.split("/") if p.startswith("{") and p.endswith("}")):
        name = part[1:-1]
        raw = raw.replace(part, example_from_path_param(name))
    if query:
        raw += "?" + "&".join(f"{item['name']}={item['example']}" for item in query)
    return raw


def build_postman_collection(endpoints):
    folders = defaultdict(list)
    for endpoint in endpoints:
        headers = [
            {"key": "Accept", "value": "application/json"},
            {"key": "X-Tenant-Key", "value": "{{tenant_key}}"},
            {"key": "X-Site-Key", "value": "{{site_key}}"},
            {"key": "X-Client-Key", "value": "{{client_key}}"},
        ]
        if endpoint["body"] is not None:
            headers.append({"key": "Content-Type", "value": "application/json"})
        request = {
            "name": endpoint["summary"],
            "request": {
                "method": endpoint["method"],
                "header": headers,
                "auth": auth_for_endpoint(endpoint["auth"]),
                "url": path_to_postman_url(endpoint["path"], endpoint["query"]),
                "description": endpoint["description"],
            },
            "response": [],
        }
        if endpoint["path"].startswith(("/endpoint/entities", "/internal/entities")):
            request["request"]["url"] = request["request"]["url"].replace(
                "{{gateway_base_url}}", "{{dynamic_service_base_url}}", 1)
        if endpoint["method"] == "GET" and endpoint["path"] in DEFINITION_LIST_PATHS:
            request["request"]["url"] = (
                "{{dynamic_service_base_url}}"
                + endpoint["path"]
                + "?page={{definition_page}}"
                + "&size={{definition_page_size}}"
                + "&sort={{definition_sort}}"
            )
            response_name = (
                "Internal definition page returned"
                if endpoint["path"].startswith("/internal/")
                else "Definition page returned"
            )
            metadata_name = (
                "Internal definition page has pagination metadata"
                if endpoint["path"].startswith("/internal/")
                else "Definition page has pagination metadata"
            )
            request["event"] = [{
                "listen": "test",
                "script": {
                    "type": "text/javascript",
                    "exec": [
                        f"pm.test('{response_name}', () => pm.response.to.have.status(200));",
                        "const page = pm.response.json();",
                        f"pm.test('{metadata_name}', () => {{",
                        "  pm.expect(page.content).to.be.an('array');",
                        "  pm.expect(page.page).to.be.a('number');",
                        "  pm.expect(page.size).to.be.a('number');",
                        "  pm.expect(page.totalElements).to.be.a('number');",
                        "  pm.expect(page.totalPages).to.be.a('number');",
                        "});",
                    ],
                },
            }]
        if endpoint["body"] is not None:
            request["request"]["body"] = {
                "mode": "raw",
                "raw": json.dumps(endpoint["body"], indent=2),
                "options": {"raw": {"language": "json"}},
            }
        if endpoint["path"] == "/api/sso/auth/login":
            request["event"] = [{
                "listen": "test",
                "script": {
                    "type": "text/javascript",
                    "exec": [
                        "const json = pm.response.json();",
                        "if (json.accessToken) pm.environment.set('access_token', json.accessToken);",
                        "if (json.refreshToken) pm.environment.set('refresh_token', json.refreshToken);",
                        "if (json.sessionId) pm.environment.set('session_id', json.sessionId);",
                    ],
                },
            }]
        folders[endpoint["service"]].append(request)

    items = [{"name": service, "item": requests} for service, requests in sorted(folders.items())]
    return {
        "info": {
            "name": "Cyan Business Platform APIs",
            "_postman_id": "d710e8f8-97c8-4c50-b7a1-api-docs",
            "description": "Generated Postman collection for platform APIs grouped by microservice. Login request stores access/refresh/session values into the active environment.",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
        },
        "item": items,
        "variable": [
            {"key": "gateway_base_url", "value": DEFAULT_GATEWAY_BASE_URL},
            {"key": "dynamic_service_base_url", "value": "http://localhost:9119"},
            {"key": "tenant_key", "value": "demo-tenant"},
            {"key": "site_key", "value": "main-site"},
            {"key": "client_key", "value": "spiffy-client"},
            {"key": "definition_page", "value": "0"},
            {"key": "definition_page_size", "value": "20"},
            {"key": "definition_sort", "value": "entityKey,asc"},
        ],
    }


def build_postman_environment():
    values = OrderedDict([
        ("gateway_base_url", DEFAULT_GATEWAY_BASE_URL),
        ("dynamic_service_base_url", "http://localhost:9119"),
        ("tenant_key", "demo-tenant"),
        ("site_key", "main-site"),
        ("client_key", "spiffy-client"),
        ("definition_page", "0"),
        ("definition_page_size", "20"),
        ("definition_sort", "entityKey,asc"),
        ("username", DEFAULT_USERNAME),
        ("password", DEFAULT_PASSWORD),
        ("access_token", ""),
        ("refresh_token", ""),
        ("session_id", ""),
        ("captcha_challenge_id", ""),
        ("ai_session_id", ""),
        ("draft_id", ""),
        ("run_id", ""),
        ("object_id", ""),
        ("record_key", "item-spiffy-bag"),
        ("transaction_key", "tx-1001"),
        ("execution_id", ""),
        ("message_key", "order-created-1001"),
        ("internal_basic_username", "internal"),
        ("internal_basic_password", "internal-secret"),
    ])
    return {
        "name": "cyan-business-platform-local",
        "values": [{"key": key, "value": value, "type": "default", "enabled": True} for key, value in values.items()],
        "_postman_variable_scope": "environment",
        "_postman_exported_at": "2025-01-01T00:00:00.000Z",
        "_postman_exported_using": "Codex API Docs Generator",
    }


def security_for(auth_type):
    if auth_type == "bearer":
        return [{"BearerAuth": []}]
    if auth_type == "basic":
        return [{"BasicAuth": []}]
    return []


def build_openapi(endpoints):
    paths = OrderedDict()
    for endpoint in endpoints:
        operations = paths.setdefault(endpoint["path"], OrderedDict())
        parameters = []
        for part in endpoint["path"].split("/"):
            if part.startswith("{") and part.endswith("}"):
                name = part[1:-1]
                parameters.append({
                    "name": name,
                    "in": "path",
                    "required": True,
                    "schema": {"type": "string"},
                    "example": example_from_path_param(name),
                })
        for item in endpoint["query"]:
            parameters.append({
                "name": item["name"],
                "in": "query",
                "required": False,
                "schema": {"type": "string"},
                "example": item["example"],
            })
        operation = {
            "tags": [endpoint["service"]],
            "summary": endpoint["summary"],
            "description": endpoint["description"] or f"{endpoint['summary']} for {endpoint['service']}.",
            "security": security_for(endpoint["auth"]),
            "responses": {
                "200": {
                    "description": "Successful response",
                    "content": {
                        "application/json": {
                            "schema": {"type": "object", "additionalProperties": True},
                            "example": endpoint["response"] if not isinstance(endpoint["response"], str) else {"value": endpoint["response"]},
                        }
                    }
                }
            }
        }
        if parameters:
            operation["parameters"] = parameters
        if endpoint["body"] is not None:
            operation["requestBody"] = {
                "required": True,
                "content": {
                    "application/json": {
                        "schema": {"type": "object", "additionalProperties": True},
                        "example": endpoint["body"],
                    }
                }
            }
        operations[endpoint["method"].lower()] = operation

    return {
        "openapi": "3.0.3",
        "info": {
            "title": "Cyan Business Platform API",
            "version": "1.0.0",
            "description": "Static OpenAPI inventory for the platform, grouped by microservice. Auth support includes Bearer JWT for endpoint/api routes and Basic Auth for internal routes.",
        },
        "servers": [{
            "url": "{gateway_base_url}",
            "description": "Gateway base URL",
            "variables": {
                "gateway_base_url": {"default": DEFAULT_GATEWAY_BASE_URL}
            }
        }],
        "tags": [{"name": service} for service in sorted({endpoint["service"] for endpoint in endpoints})],
        "components": {
            "securitySchemes": {
                "BearerAuth": {"type": "http", "scheme": "bearer", "bearerFormat": "JWT"},
                "BasicAuth": {"type": "http", "scheme": "basic"},
            }
        },
        "paths": paths,
    }


def build_swagger_index(service_names):
    urls = [{"name": "Platform - All Services", "url": "./cyan-business-platform.openapi.json"}]
    urls.extend({
        "name": service,
        "url": f"./services/{slugify(service)}.openapi.json",
    } for service in service_names)
    return """<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Cyan Business Platform Swagger</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css">
  <style>
    body { margin: 0; background: #f5f7fb; }
    .topbar { display: none; }
  </style>
</head>
<body>
  <div id="swagger-ui"></div>
  <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
  <script>
    window.ui = SwaggerUIBundle({
      urls: __URLS__,
      dom_id: '#swagger-ui',
      deepLinking: true,
      persistAuthorization: true,
      displayRequestDuration: true,
      docExpansion: 'list',
      tryItOutEnabled: true
    });
  </script>
</body>
</html>
""".replace("__URLS__", json.dumps(urls, indent=6))


def build_readme(endpoints):
    services = sorted({endpoint["service"] for endpoint in endpoints})
    return "\n".join([
        "# API Docs",
        "",
        "Generated assets:",
        "- `docs/postman/cyan-business-platform.postman_collection.json`",
        "- `docs/postman/cyan-business-platform.postman_environment.template.json`",
        "- `docs/swagger/cyan-business-platform.openapi.json`",
        "- `docs/swagger/services/*.openapi.json`",
        "- `docs/swagger/index.html`",
        "",
        "Usage:",
        "1. Import the Postman collection and environment template.",
        "2. Run `SSO / Login` first. Its test script stores `access_token`, `refresh_token`, and `session_id` in the environment.",
        "3. Set `dynamic_service_base_url` to the dynamic service under test; it defaults to local `bpm-service` on port `9119`.",
        "4. Definition list requests use `definition_page`, `definition_page_size`, and `definition_sort`; their tests verify the pagination envelope.",
        "5. Open `docs/swagger/index.html` in a browser, then use Swagger's `Authorize` button with either a bearer token or internal basic credentials.",
        "6. Use the Swagger spec selector to switch between the full platform inventory and per-service specs.",
        "",
        "Coverage tags:",
        *[f"- `{service}`" for service in services],
        "",
        "Regenerate:",
        "- `python3 scripts/generate_api_docs.py`",
    ])


def slugify(value):
    return "".join(ch.lower() if ch.isalnum() else "-" for ch in value).strip("-")


def main():
    endpoints = build_endpoints()
    POSTMAN_DIR.mkdir(parents=True, exist_ok=True)
    SWAGGER_DIR.mkdir(parents=True, exist_ok=True)
    SWAGGER_SERVICES_DIR.mkdir(parents=True, exist_ok=True)

    (POSTMAN_DIR / "cyan-business-platform.postman_collection.json").write_text(
        json.dumps(build_postman_collection(endpoints), indent=2) + "\n",
        encoding="utf-8",
    )
    (POSTMAN_DIR / "cyan-business-platform.postman_environment.template.json").write_text(
        json.dumps(build_postman_environment(), indent=2) + "\n",
        encoding="utf-8",
    )
    (SWAGGER_DIR / "cyan-business-platform.openapi.json").write_text(
        json.dumps(build_openapi(endpoints), indent=2) + "\n",
        encoding="utf-8",
    )
    service_names = sorted({endpoint["service"] for endpoint in endpoints})
    for service in service_names:
        service_endpoints = [endpoint for endpoint in endpoints if endpoint["service"] == service]
        (SWAGGER_SERVICES_DIR / f"{slugify(service)}.openapi.json").write_text(
            json.dumps(build_openapi(service_endpoints), indent=2) + "\n",
            encoding="utf-8",
        )
    (SWAGGER_DIR / "index.html").write_text(build_swagger_index(service_names), encoding="utf-8")
    (ROOT / "docs" / "API_DOCS.md").write_text(build_readme(endpoints) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
