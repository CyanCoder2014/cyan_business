package com.cyancoder.tax_pay_sys_service.modules.transfer.interfaces;

import com.cyancoder.tax_pay_sys_service.modules.transfer.dto.PacketDto;
import com.cyancoder.tax_pay_sys_service.modules.transfer.exception.TaxApiException;

import java.util.List;

public interface Encrypter {

    void encrypt(List<PacketDto> packets) throws TaxApiException;
}
