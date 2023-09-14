package com.cyancoder.tax_pay_sys_service.modules.content.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cyancoder.tax_pay_sys_service.modules.content.config.PacketType;
import com.cyancoder.tax_pay_sys_service.modules.content.dto.*;
import com.cyancoder.tax_pay_sys_service.modules.transfer.api.TransferApi;
import com.cyancoder.tax_pay_sys_service.modules.transfer.config.TransferConstants;
import com.cyancoder.tax_pay_sys_service.modules.transfer.dto.AsyncResponseModel;
import com.cyancoder.tax_pay_sys_service.modules.transfer.dto.PacketDto;
import com.cyancoder.tax_pay_sys_service.modules.transfer.dto.SyncResponseModel;
import com.cyancoder.tax_pay_sys_service.modules.transfer.exception.TaxApiException;
import com.cyancoder.tax_pay_sys_service.modules.transfer.impl.encrypter.DefaultEncrypter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DefaultTaxApiClient implements TaxApi {

    private final TransferApi transferApi;

    private final String clientId;

    private TokenModel token;

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public DefaultTaxApiClient(TransferApi transferApi, String clientId) {
        this.transferApi = transferApi;
        this.clientId = clientId;
    }

    @Override
    public TokenModel requestToken() throws TaxApiException {
        GetTokenDto data = new GetTokenDto();
        data.setUsername(clientId);
        PacketDto packetDto = packetDtoBuilder(data, PacketType.PACKET_TYPE_GET_TOKEN);
        SyncResponseModel response = transferApi.sendPacket(packetDto, null, false, false);

        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            throw new TaxApiException(response.getErrors().get(0).getDetail());
        }

        Object responseData = response.getResult().getData();
        TokenModel tokenModel = mapper.convertValue(responseData, TokenModel.class);

        if (tokenModel != null) {
            this.token = tokenModel;
        }

        return tokenModel;
    }

    @Override
    public void setAuthInfo(String token){
        this.token = new TokenModel();
        this.token.setToken(token);
    }

    @Override
    public AsyncResponseModel sendInvoices(List<InvoiceDto> invoices) throws TaxApiException {
        ArrayList<PacketDto> packets = new ArrayList<>();
        for (InvoiceDto invoiceDto : invoices) {
            packets.add(packetDtoBuilder(invoiceDto, "INVOICE.V01"));
        }

        HashMap<String, String> headers = new HashMap<>();
        if (token != null) {
            headers.put(TransferConstants.AUTHORIZATION_HEADER, "Bearer " + token.getToken());
        }
        return transferApi.sendPackets(packets, headers, true, true);
    }

    @Override
    public ServerInformationModel getServerInformation() throws TaxApiException {
        PacketDto packetDto = packetDtoBuilder(null, PacketType.PACKET_TYPE_GET_SERVER_INFORMATION);
        SyncResponseModel response = transferApi.sendPacket(packetDto, null, false, false);

        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            throw new TaxApiException(response.getErrors().get(0).getDetail());
        }

        Object responseData = response.getResult().getData();
        ServerInformationModel si = mapper.convertValue(responseData, ServerInformationModel.class);

        if (transferApi.getConfig().getEncrypter() == null && si.getPublicKeys() != null && !si.getPublicKeys().isEmpty()) {
            KeyDto key = si.getPublicKeys().get(0);
            DefaultEncrypter encrypter = new DefaultEncrypter(key.getKey(), key.getId());
            transferApi.getConfig().encrypter(encrypter);
        }

        return si;
    }

    @Override
    public List<InquiryResultModel> inquiryByUidAndFiscalId(List<UidAndFiscalId> uidAndFiscalIds) throws TaxApiException {
        PacketDto packetDto = packetDtoBuilder(uidAndFiscalIds, PacketType.PACKET_TYPE_INQUIRY_BY_UID);
        HashMap<String, String> headers = new HashMap<>();
        if (token != null) {
            headers.put(TransferConstants.AUTHORIZATION_HEADER, "Bearer " + token.getToken());
        }
        SyncResponseModel response = transferApi.sendPacket(packetDto, headers, false, false);

        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            throw new TaxApiException(response.getErrors().get(0).getDetail());
        }

        Object responseData = response.getResult().getData();
        return mapper.convertValue(responseData, new TypeReference<List<InquiryResultModel>>() {
        });
    }

    @Override
    public List<InquiryResultModel> inquiryByTime(String persianTime) throws TaxApiException {
        InquiryByTimeDto data = new InquiryByTimeDto();
        data.setTime(persianTime);
        PacketDto packetDto = packetDtoBuilder(data, PacketType.PACKET_TYPE_INQUIRY_BY_TIME);
        HashMap<String, String> headers = new HashMap<>();
        if (token != null) {
            headers.put(TransferConstants.AUTHORIZATION_HEADER, "Bearer " + token.getToken());
        }
        SyncResponseModel response = transferApi.sendPacket(packetDto, headers, false, false);

        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            throw new TaxApiException(response.getErrors().get(0).getDetail());
        }

        Object responseData = response.getResult().getData();
        return mapper.convertValue(responseData, new TypeReference<List<InquiryResultModel>>() {
        });
    }

    @Override
    public List<InquiryResultModel> inquiryByTimeRange(String startDatePersian, String toDatePersian) throws TaxApiException {
        InquiryByTimeRangeDto data = new InquiryByTimeRangeDto();
        data.setStartDate(startDatePersian);
        data.setEndDate(toDatePersian);
        PacketDto packetDto = packetDtoBuilder(data, PacketType.PACKET_TYPE_INQUIRY_BY_TIME_RANGE);
        HashMap<String, String> headers = new HashMap<>();
        if (token != null) {
            headers.put(TransferConstants.AUTHORIZATION_HEADER, "Bearer " + token.getToken());
        }
        SyncResponseModel response = transferApi.sendPacket(packetDto, headers, false, false);

        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            throw new TaxApiException(response.getErrors().get(0).getDetail());
        }

        Object responseData = response.getResult().getData();
        return mapper.convertValue(responseData, new TypeReference<List<InquiryResultModel>>() {
        });
    }

    @Override
    public List<InquiryResultModel> inquiryByReferenceId(List<String> referenceIds) throws TaxApiException {
        InquiryByReferenceNumberDto data = new InquiryByReferenceNumberDto();
        data.setReferenceNumber(referenceIds);
        PacketDto packetDto = packetDtoBuilder(data, PacketType.PACKET_TYPE_INQUIRY_BY_REFERENCE_NUMBER);
        HashMap<String, String> headers = new HashMap<>();
        if (token != null) {
            headers.put(TransferConstants.AUTHORIZATION_HEADER, "Bearer " + token.getToken());
        }
        SyncResponseModel response = transferApi.sendPacket(packetDto, headers, false, false);

        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            throw new TaxApiException(response.getErrors().get(0).getDetail());
        }

        Object responseData = response.getResult().getData();
        return mapper.convertValue(responseData, new TypeReference<List<InquiryResultModel>>() {
        });
    }

    @Override
    public FiscalInformationModel getFiscalInformation(String fiscalId) throws TaxApiException {
        PacketDto packetDto = packetDtoBuilder(fiscalId, PacketType.PACKET_TYPE_GET_FISCAL_INFORMATION);
        HashMap<String, String> headers = new HashMap<>();
        if (token != null) {
            headers.put(TransferConstants.AUTHORIZATION_HEADER, "Bearer " + token.getToken());
        }
        SyncResponseModel response = transferApi.sendPacket(packetDto, headers, false, false);

        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            throw new TaxApiException(response.getErrors().get(0).getDetail());
        }

        Object responseData = response.getResult().getData();
        return mapper.convertValue(responseData, FiscalInformationModel.class);
    }

    @Override
    public SearchResultModel<ServiceStuffModel> getServiceStuffList(SearchDto searchDTO) throws TaxApiException {
        PacketDto packetDto = packetDtoBuilder(searchDTO, PacketType.PACKET_TYPE_GET_SERVICE_STUFF_LIST);
        HashMap<String, String> headers = new HashMap<>();
        if (token != null) {
            headers.put(TransferConstants.AUTHORIZATION_HEADER, "Bearer " + token.getToken());
        }
        SyncResponseModel response = transferApi.sendPacket(packetDto, headers, false, false);

        if (response == null) {
            return null;
        }

        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            throw new TaxApiException(response.getErrors().get(0).getDetail());
        }

        Object responseData = response.getResult().getData();
        return mapper.convertValue(responseData, new TypeReference<SearchResultModel<ServiceStuffModel>>() {
        });
    }

    @Override
    public EconomicCodeModel getEconomicCodeInformation(String economicCode) throws TaxApiException {
        EconomicCodeDto data = new EconomicCodeDto();
        data.setEconomicCode(economicCode);
        PacketDto packetDto = packetDtoBuilder(data, PacketType.PACKET_TYPE_GET_ECONOMIC_CODE_INFORMATION);
        HashMap<String, String> headers = new HashMap<>();
        if (token != null) {
            headers.put(TransferConstants.AUTHORIZATION_HEADER, "Bearer " + token.getToken());
        }
        SyncResponseModel response = transferApi.sendPacket(packetDto, headers, false, false);

        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            throw new TaxApiException(response.getErrors().get(0).getDetail());
        }

        Object responseData = response.getResult().getData();
        return mapper.convertValue(responseData, EconomicCodeModel.class);
    }



    private PacketDto packetDtoBuilder(Object data, String packetType) {
        PacketDto packetDto = new PacketDto();
        packetDto.setUid(UUID.randomUUID().toString());
        packetDto.setPacketType(packetType);
        packetDto.setFiscalId(clientId);
        packetDto.setData(data);
        packetDto.setRetry(false);
        return packetDto;
    }

    public TokenModel getToken() {
        return token;
    }

    public void setToken(TokenModel token) {
        this.token = token;
    }
}
