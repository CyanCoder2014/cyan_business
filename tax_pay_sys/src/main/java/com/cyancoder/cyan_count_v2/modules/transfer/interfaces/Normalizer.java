package main.java.com.cyancoder.tax_pay_sys_service.modules.transfer.interfaces;

import java.util.Map;

public interface Normalizer {

    String normalize(Object data, Map<String, String> headers);
}
