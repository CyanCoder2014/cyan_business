package com.cyancoder.payment.config;

import com.cyancoder.payment.domain.PaymentFlowType;
import com.cyancoder.payment.domain.PaymentProviderCode;
import com.cyancoder.payment.domain.PaymentRegion;
import com.cyancoder.payment.dto.PaymentMethodRequest;
import com.cyancoder.payment.service.PaymentMethodAdminService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableConfigurationProperties(PaymentServiceProperties.class)
public class PaymentBootstrapConfig {

    @Bean
    CommandLineRunner seedPaymentMethods(PaymentMethodAdminService paymentMethodAdminService) {
        return args -> seedIfMissing(paymentMethodAdminService);
    }

    private void seedIfMissing(PaymentMethodAdminService service) {
        seed(service, new PaymentMethodRequest("tejarat-default", "Tejarat Gateway", PaymentProviderCode.TEJARAT, PaymentRegion.IRANIAN, PaymentFlowType.REDIRECT, true, true, 10, Set.of("IRR"), template("merchantId", "demo-tejarat", "terminalId", "terminal-01", "gatewayUrl", "https://ikc.shaparak.ir/TPayment/Payment/index", "mockMode", true), "Bank Tejarat online payment"));
        seed(service, new PaymentMethodRequest("sep-default", "SEP Gateway", PaymentProviderCode.SEP, PaymentRegion.IRANIAN, PaymentFlowType.REDIRECT, true, true, 20, Set.of("IRR"), template("merchantId", "demo-sep", "terminalId", "terminal-02", "gatewayUrl", "https://sep.shaparak.ir/OnlinePG/OnlinePG", "mockMode", true), "SEP online payment"));
        seed(service, new PaymentMethodRequest("zarinpal-default", "Zarinpal", PaymentProviderCode.ZARINPAL, PaymentRegion.IRANIAN, PaymentFlowType.REDIRECT, true, true, 30, Set.of("IRR"), template("merchantId", "demo-zarinpal", "gatewayUrl", "https://www.zarinpal.com/pg/StartPay/", "mockMode", true), "Zarinpal payment gateway"));
        seed(service, new PaymentMethodRequest("payir-default", "Pay.ir", PaymentProviderCode.PAY_IR, PaymentRegion.IRANIAN, PaymentFlowType.REDIRECT, true, true, 40, Set.of("IRR"), template("apiKey", "demo-payir", "gatewayUrl", "https://pay.ir/pg/", "mockMode", true), "Pay.ir payment gateway"));
        seed(service, new PaymentMethodRequest("paypal-default", "PayPal Checkout", PaymentProviderCode.PAYPAL, PaymentRegion.INTERNATIONAL, PaymentFlowType.REDIRECT, true, true, 50, Set.of("USD", "EUR"), template("clientId", "demo-paypal", "environment", "sandbox", "gatewayUrl", "https://www.paypal.com/checkoutnow", "mockMode", true), "PayPal online checkout"));
        seed(service, new PaymentMethodRequest("visa-default", "Visa Checkout", PaymentProviderCode.VISA, PaymentRegion.INTERNATIONAL, PaymentFlowType.REDIRECT, true, true, 60, Set.of("USD", "EUR", "AED"), template("merchantId", "demo-visa", "acquirer", "demo-acquirer", "gatewayUrl", "https://visa.example.com/checkout", "mockMode", true), "Visa network checkout through PSP"));
        seed(service, new PaymentMethodRequest("mastercard-default", "Mastercard Checkout", PaymentProviderCode.MASTERCARD, PaymentRegion.INTERNATIONAL, PaymentFlowType.REDIRECT, true, true, 70, Set.of("USD", "EUR", "AED"), template("merchantId", "demo-mastercard", "acquirer", "demo-acquirer", "gatewayUrl", "https://mastercard.example.com/checkout", "mockMode", true), "Mastercard checkout through PSP"));
    }

    private void seed(PaymentMethodAdminService service, PaymentMethodRequest request) {
        if (service.findOptionalEntity(request.methodKey()).isEmpty()) {
            service.create(request);
        }
    }

    private Map<String, Object> template(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        List<Object> list = List.of(values);
        for (int i = 0; i < list.size(); i += 2) {
            map.put(String.valueOf(list.get(i)), list.get(i + 1));
        }
        return map;
    }
}
