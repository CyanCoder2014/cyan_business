package com.cyancoder.taxpaysys.modules.transfer.interfaces;

import com.cyancoder.taxpaysys.modules.transfer.exception.TaxApiException;

public interface Signatory {

    String sign(String data) throws TaxApiException;

    String getKeyId();
}
