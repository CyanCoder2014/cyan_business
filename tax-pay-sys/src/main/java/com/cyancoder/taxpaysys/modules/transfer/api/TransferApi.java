package com.cyancoder.taxpaysys.modules.transfer.api;

import com.cyancoder.taxpaysys.modules.transfer.config.ApiConfig;
import com.cyancoder.taxpaysys.modules.transfer.dto.AsyncResponseModel;
import com.cyancoder.taxpaysys.modules.transfer.dto.PacketDto;
import com.cyancoder.taxpaysys.modules.transfer.dto.SyncResponseModel;

import java.util.List;
import java.util.Map;

public interface TransferApi {

    ApiConfig getConfig();

    AsyncResponseModel sendPackets(List<PacketDto> packets, Map<String, String> headers, boolean encrypt, boolean sign);

    SyncResponseModel sendPacket(PacketDto packet, Map<String, String> headers, boolean encrypt, boolean sign);
}
