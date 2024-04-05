package com.cyancoder.taxpaysys.modules.transfer.interfaces;

import com.cyancoder.taxpaysys.modules.transfer.dto.PacketDto;
import com.cyancoder.taxpaysys.modules.transfer.exception.TaxApiException;

import java.util.List;

public interface Encrypter {

    void encrypt(List<PacketDto> packets) throws TaxApiException;
}
