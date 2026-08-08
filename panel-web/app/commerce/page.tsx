"use client";

import { useEffect, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { useScopeAccess } from "@/components/scope-access-provider";
import { createDefinitionFromTemplate, listRecords, submitRecord } from "@/lib/dynamic-api";
import { createPaymentMethod, initiatePaymentSession, listPaymentMethods } from "@/lib/service-api";
import type { DynamicEntityRecord, PaymentMethodAdmin, PaymentSessionResponse } from "@/lib/types";

export default function CommercePage() {
  const { tenantKey: activeTenantKey, siteKey: activeSiteKey } = useScopeAccess();
  const [tenantKey, setTenantKey] = useState("");
  const [siteKey, setSiteKey] = useState("");
  const [cartKey, setCartKey] = useState("default-cart");
  const [checkoutKey, setCheckoutKey] = useState("default-checkout");
  const [promotionKey, setPromotionKey] = useState("launch10");
  const [taxKey, setTaxKey] = useState("default-vat");
  const [methodKey, setMethodKey] = useState("sandbox-gateway");
  const [paymentMethods, setPaymentMethods] = useState<PaymentMethodAdmin[]>([]);
  const [sessionResult, setSessionResult] = useState<PaymentSessionResponse | null>(null);
  const [carts, setCarts] = useState<DynamicEntityRecord[]>([]);
  const [checkouts, setCheckouts] = useState<DynamicEntityRecord[]>([]);
  const [promotions, setPromotions] = useState<DynamicEntityRecord[]>([]);
  const [taxRules, setTaxRules] = useState<DynamicEntityRecord[]>([]);
  const [status, setStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setTenantKey(activeTenantKey ?? "");
    setSiteKey(activeSiteKey ?? "");
  }, [activeSiteKey, activeTenantKey]);

  async function refresh() {
    if (!tenantKey) return;
    await Promise.all([
      createDefinitionFromTemplate("cart-service", "shopping-cart", "shopping-cart", { tenantKey, siteKey }).catch(() => null),
      createDefinitionFromTemplate("checkout-service", "checkout-session", "checkout-session", { tenantKey, siteKey }).catch(() => null),
      createDefinitionFromTemplate("pricing-promotion-service", "promotion-rule", "promotion-rule", { tenantKey, siteKey }).catch(() => null),
      createDefinitionFromTemplate("pricing-promotion-service", "tax-rule", "tax-rule", { tenantKey, siteKey }).catch(() => null)
    ]);
    const [cartItems, checkoutItems, promotionItems, taxItems, methods] = await Promise.all([
      listRecords("cart-service", "shopping-cart", { tenantKey, siteKey }).catch(() => []),
      listRecords("checkout-service", "checkout-session", { tenantKey, siteKey }).catch(() => []),
      listRecords("pricing-promotion-service", "promotion-rule", { tenantKey, siteKey }).catch(() => []),
      listRecords("pricing-promotion-service", "tax-rule", { tenantKey, siteKey }).catch(() => []),
      listPaymentMethods().catch(() => [])
    ]);
    setCarts(cartItems);
    setCheckouts(checkoutItems);
    setPromotions(promotionItems);
    setTaxRules(taxItems);
    setPaymentMethods(methods);
  }

  useEffect(() => {
    refresh().catch((error) => setStatus(error instanceof Error ? error.message : "Failed to load commerce builder"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tenantKey, siteKey]);

  async function seedCommerce() {
    setLoading(true);
    setStatus(null);
    try {
      await submitRecord("cart-service", "shopping-cart", cartKey, {
        cartKey,
        sessionKey: `${cartKey}-session`,
        customerRef: {
          service: "crm-service",
          entityKey: "customer",
          recordKey: "guest-demo"
        },
        currency: "IRR",
        status: "ACTIVE",
        items: [],
        pricing: {
          subtotal: 0,
          discountTotal: 0,
          shippingTotal: 0,
          taxTotal: 0,
          grandTotal: 0,
          currency: "IRR",
          breakdown: []
        }
      }, { tenantKey, siteKey });
      await submitRecord("checkout-service", "checkout-session", checkoutKey, {
        checkoutKey,
        cartRef: {
          service: "cart-service",
          entityKey: "shopping-cart",
          recordKey: cartKey
        },
        customer: {
          fullName: "Guest Demo",
          email: "guest@example.com"
        },
        billingAddress: {
          country: "IR",
          city: "Tehran",
          line1: "Demo billing"
        },
        shippingAddress: {
          country: "IR",
          city: "Tehran",
          line1: "Demo shipping"
        },
        shippingOption: {
          methodKey: "standard",
          carrier: "internal",
          price: 0
        },
        totals: {
          subtotal: 0,
          discountTotal: 0,
          shippingTotal: 0,
          taxTotal: 0,
          grandTotal: 0,
          currency: "IRR",
          breakdown: []
        },
        paymentPreference: {
          methodKey,
          region: "GLOBAL",
          gatewayType: "MANUAL"
        },
        status: "CREATED",
        notificationStatus: "PENDING"
      }, { tenantKey, siteKey });
      await submitRecord("pricing-promotion-service", "promotion-rule", promotionKey, {
        promotionKey,
        code: "LAUNCH10",
        discountType: "PERCENTAGE",
        discountValue: 10,
        conditions: [],
        targets: [],
        stacking: {
          exclusive: "false",
          priority: 10
        },
        status: "ACTIVE"
      }, { tenantKey, siteKey });
      await submitRecord("pricing-promotion-service", "tax-rule", taxKey, {
        taxRuleKey: taxKey,
        calculationMode: "PERCENTAGE",
        rate: 9,
        jurisdictions: [{ country: "IR", city: "Tehran" }],
        appliesTo: [{ scope: "ORDER", targetKey: "default" }],
        status: "ACTIVE"
      }, { tenantKey, siteKey });
      await createPaymentMethod({
        methodKey,
        displayName: "Sandbox Gateway",
        providerCode: "PAYPAL",
        region: "INTERNATIONAL",
        flowType: "REDIRECT",
        enabled: true,
        active: true,
        priorityOrder: 10,
        supportedCurrencies: ["IRR"],
        configuration: {
          callbackMode: "manual"
        },
        description: "Panel-seeded method"
      }).catch(() => null);
      await refresh();
      setStatus("Commerce records and payment method are ready.");
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to seed commerce records");
    } finally {
      setLoading(false);
    }
  }

  async function createSession() {
    setLoading(true);
    setStatus(null);
    try {
      const session = await initiatePaymentSession({
        paymentMethodKey: methodKey,
        orderKey: `order-${Date.now().toString(36)}`,
        customerKey: "guest-demo",
        relatedService: "checkout-service",
        relatedEntityType: "checkout-session",
        relatedEntityKey: checkoutKey,
        amount: 250000,
        currency: "IRR",
        callbackUrl: "https://example.com/payment/callback",
        successUrl: "https://example.com/payment/success",
        failureUrl: "https://example.com/payment/failure",
        metaData: { source: "panel-commerce-builder", tenantKey, siteKey }
      });
      setSessionResult(session);
      setStatus(`Payment session ${session.transactionKey ?? "created"} initiated.`);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : "Failed to initiate payment session");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppShell title="Commerce Builder" subtitle="Seed cart, checkout, pricing, and payment runtime records from one public-experience workspace.">
      <div className="studio-grid">
        <section className="panel rail">
          <div className="form-grid">
            <div className="field-grid">
              <div className="field">
                <label>Tenant key</label>
                <input value={tenantKey} onChange={(event) => setTenantKey(event.target.value)} />
              </div>
              <div className="field">
                <label>Site key</label>
                <input value={siteKey} onChange={(event) => setSiteKey(event.target.value)} />
              </div>
            </div>
            <div className="field-grid">
              <div className="field">
                <label>Cart key</label>
                <input value={cartKey} onChange={(event) => setCartKey(event.target.value)} />
              </div>
              <div className="field">
                <label>Checkout key</label>
                <input value={checkoutKey} onChange={(event) => setCheckoutKey(event.target.value)} />
              </div>
            </div>
            <div className="field-grid">
              <div className="field">
                <label>Promotion key</label>
                <input value={promotionKey} onChange={(event) => setPromotionKey(event.target.value)} />
              </div>
              <div className="field">
                <label>Tax key</label>
                <input value={taxKey} onChange={(event) => setTaxKey(event.target.value)} />
              </div>
            </div>
            <div className="field">
              <label>Payment method key</label>
              <input value={methodKey} onChange={(event) => setMethodKey(event.target.value)} />
            </div>
            <div className="hero-actions">
              <button type="button" className="btn" onClick={seedCommerce} disabled={loading}>Seed commerce runtime</button>
              <button type="button" className="ghost-btn" onClick={createSession} disabled={loading}>Initiate payment session</button>
            </div>
            {status ? <div className="ai-banner">{status}</div> : null}
          </div>
        </section>
        <aside className="sidebar">
          <section className="panel rail"><p className="section-title">Carts</p><pre className="json-view">{JSON.stringify(carts, null, 2)}</pre></section>
          <section className="panel rail"><p className="section-title">Checkouts</p><pre className="json-view">{JSON.stringify(checkouts, null, 2)}</pre></section>
          <section className="panel rail"><p className="section-title">Promotions / Tax</p><pre className="json-view">{JSON.stringify({ promotions, taxRules }, null, 2)}</pre></section>
          <section className="panel rail"><p className="section-title">Payment</p><pre className="json-view">{JSON.stringify({ paymentMethods, sessionResult }, null, 2)}</pre></section>
        </aside>
      </div>
    </AppShell>
  );
}
