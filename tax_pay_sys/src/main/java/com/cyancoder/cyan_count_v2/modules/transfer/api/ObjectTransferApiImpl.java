package main.java.com.cyancoder.tax_pay_sys_service.modules.transfer.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cyancoder.tax_pay_sys_service.modules.transfer.config.ApiConfig;
import com.cyancoder.tax_pay_sys_service.modules.transfer.config.TransferConstants;
import com.cyancoder.tax_pay_sys_service.modules.transfer.dto.*;
import com.cyancoder.tax_pay_sys_service.modules.transfer.exception.TaxApiException;
import com.cyancoder.tax_pay_sys_service.modules.transfer.http.HttpResponse;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

public class ObjectTransferApiImpl implements TransferApi {

    private static Logger logger = Logger.getLogger(ObjectTransferApiImpl.class.getName());

    private final ApiConfig config;

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public ObjectTransferApiImpl(ApiConfig config) {
        this.config = config;
    }

    @Override
    public ApiConfig getConfig() {
        return config;
    }

    @Override
    public AsyncResponseModel sendPackets(List<PacketDto> packets,
                                          Map<String, String> headers,
                                          boolean encrypt,
                                          boolean sign) {

        if (packets == null || packets.isEmpty()) {
            return null;
        }
        if (headers == null) {
            headers = new HashMap<>();
        }

        fillEssentialHeaders(headers);

        if (sign) {
            packets.forEach(this::signPacket);
        }

        if (encrypt) {
            config.getEncrypter().encrypt(packets);
        }

        AsyncRequestDto request = new AsyncRequestDto();

        List<Object> packetObj = new ArrayList<>();
        for (PacketDto packetDto : packets) {
            if (packetDto.getSignatureKeyId() != null) {
                packetObj.add(packetDto);
            } else {
                packetObj.add(new PacketDtoWithoutSignatureKeyId(packetDto));
            }
        }

        String normalizedJson = config.getNormalizer().normalize(packetObj, headers);
        request.setSignature(config.getSignatory().sign(normalizedJson));
        if (config.getSignatory().getKeyId() != null) {
            request.setSignatureKeyId(config.getSignatory().getKeyId());
        }
        request.setPackets(packets);

        String sendData;
        try {
            sendData = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new TaxApiException("error in pars request json", e);
        }

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(config.getBaseUrl()).append('/');
        if (config.getApiVersion() != null) {
            urlBuilder.append(config.getApiVersion()).append('/');
        }
        urlBuilder.append("async/").append(config.getPriorityLevel() == PriorityLevel.HIGH ? "fast-enqueue" : "normal-enqueue");
        HttpResponse httpResponse = config.getHttpRequestSender().sendPostRequest(urlBuilder.toString(), sendData, headers);
        try {
            return mapper.readValue(httpResponse.getBody(), AsyncResponseModel.class);
        } catch (JsonProcessingException e) {
            throw new TaxApiException("error in pars response json", e);
        }
    }

    private void signPacket(PacketDto packet) {
        String packetNormalize = config.getNormalizer().normalize(packet.getData(), null);
        String packetSignature = config.getSignatory().sign(packetNormalize);
        packet.setDataSignature(packetSignature);
        packet.setSignatureKeyId(config.getSignatory().getKeyId());
    }

    @Override
    public SyncResponseModel sendPacket(PacketDto packet,
                                        Map<String, String> headers,
                                        boolean encrypt,
                                        boolean sign) {
        if (packet == null) {
            return null;
        }
        if (headers == null) {
            headers = new HashMap<>();
        }
        fillEssentialHeaders(headers);

        if (sign) {
            signPacket(packet);
        }

        if (encrypt) {
            config.getEncrypter().encrypt(Collections.singletonList(packet));
        }

        SyncRequestDto request = new SyncRequestDto();
        String normalizedJson;
        if (packet.getSignatureKeyId() != null) {
            normalizedJson = config.getNormalizer().normalize(packet, headers);
        } else {
            normalizedJson = config.getNormalizer().normalize(new PacketDtoWithoutSignatureKeyId(packet), headers);
        }

        request.setSignature(config.getSignatory().sign(normalizedJson));
        if (config.getSignatory().getKeyId() != null) {
            request.setSignatureKeyId(config.getSignatory().getKeyId());
        }
        request.setPacket(packet);

        String sendData;
        try {
            sendData = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new TaxApiException("error in pars request json", e);
        }

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(config.getBaseUrl()).append('/');
        if (config.getApiVersion() != null) {
            urlBuilder.append(config.getApiVersion()).append('/');
        }
        urlBuilder.append("sync/").append(packet.getPacketType().toUpperCase());

        HttpResponse httpResponse = config.getHttpRequestSender().sendPostRequest(urlBuilder.toString(), sendData, headers);

        if (httpResponse.getBody() == null || httpResponse.getBody().equals("")) {
            return null;
        }

        try {
            return mapper.readValue(httpResponse.getBody(), SyncResponseModel.class);
        } catch (JsonProcessingException e) {
            throw new TaxApiException("error in pars response json", e);
        }
    }

    private void fillEssentialHeaders(Map<String, String> essentialHeaders) {
        long now = Instant.now().toEpochMilli();

        if (!essentialHeaders.containsKey(TransferConstants.REQUEST_TRACE_ID_HEADER)) {
            essentialHeaders.put(TransferConstants.REQUEST_TRACE_ID_HEADER, UUID.randomUUID().toString());
        }
        if (!essentialHeaders.containsKey(TransferConstants.TIMESTAMP_HEADER)) {
            essentialHeaders.put(TransferConstants.TIMESTAMP_HEADER, String.valueOf(now));
        }
    }

    private class PacketDtoWithoutSignatureKeyId {

        private String uid;

        private String packetType;

        private Boolean retry;

        private Object data;

        private String encryptionKeyId;

        private String symmetricKey;

        private String iv;

        private String fiscalId;

        private String dataSignature;

        public PacketDtoWithoutSignatureKeyId(PacketDto packetDto) {
            this.uid = packetDto.getUid();
            this.packetType = packetDto.getPacketType();
            this.retry = packetDto.getRetry();
            this.data = packetDto.getData();
            this.encryptionKeyId = packetDto.getEncryptionKeyId();
            this.symmetricKey = packetDto.getSymmetricKey();
            this.iv = packetDto.getIv();
            this.fiscalId = packetDto.getFiscalId();
            this.dataSignature = packetDto.getDataSignature();
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getPacketType() {
            return packetType;
        }

        public void setPacketType(String packetType) {
            this.packetType = packetType;
        }

        public Boolean getRetry() {
            return retry;
        }

        public void setRetry(Boolean retry) {
            this.retry = retry;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public String getEncryptionKeyId() {
            return encryptionKeyId;
        }

        public void setEncryptionKeyId(String encryptionKeyId) {
            this.encryptionKeyId = encryptionKeyId;
        }

        public String getSymmetricKey() {
            return symmetricKey;
        }

        public void setSymmetricKey(String symmetricKey) {
            this.symmetricKey = symmetricKey;
        }

        public String getIv() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv = iv;
        }

        public String getFiscalId() {
            return fiscalId;
        }

        public void setFiscalId(String fiscalId) {
            this.fiscalId = fiscalId;
        }

        public String getDataSignature() {
            return dataSignature;
        }

        public void setDataSignature(String dataSignature) {
            this.dataSignature = dataSignature;
        }
    }
}
