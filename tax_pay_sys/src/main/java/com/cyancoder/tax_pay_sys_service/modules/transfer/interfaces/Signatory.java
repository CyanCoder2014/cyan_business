package com.cyancoder.tax_pay_sys_service.modules.transfer.interfaces;

import com.cyancoder.tax_pay_sys_service.modules.transfer.exception.TaxApiException;

public interface Signatory {

    String sign(String data) throws TaxApiException;

    String getKeyId();
}
