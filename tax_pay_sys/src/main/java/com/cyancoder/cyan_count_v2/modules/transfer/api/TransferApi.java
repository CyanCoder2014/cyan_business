package main.java.com.cyancoder.tax_pay_sys_service.modules.transfer.api;

import com.cyancoder.tax_pay_sys_service.modules.transfer.config.ApiConfig;
import com.cyancoder.tax_pay_sys_service.modules.transfer.dto.AsyncResponseModel;
import com.cyancoder.tax_pay_sys_service.modules.transfer.dto.PacketDto;
import com.cyancoder.tax_pay_sys_service.modules.transfer.dto.SyncResponseModel;

import java.util.List;
import java.util.Map;

public interface TransferApi {

    ApiConfig getConfig();

    AsyncResponseModel sendPackets(List<PacketDto> packets, Map<String, String> headers, boolean encrypt, boolean sign);

    SyncResponseModel sendPacket(PacketDto packet, Map<String, String> headers, boolean encrypt, boolean sign);
}
