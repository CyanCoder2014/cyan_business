package main.java.com.cyancoder.tax_pay_sys_service.modules.content.dto;


import java.util.List;


public class ServerInformationModel {

    private long serverTime;

    private List<KeyDto> publicKeys;

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public List<KeyDto> getPublicKeys() {
        return publicKeys;
    }

    public void setPublicKeys(List<KeyDto> publicKeys) {
        this.publicKeys = publicKeys;
    }

    @Override
    public String toString() {
        return "ServerInformationModel{" +
                "serverTime=" + serverTime +
                ", publicKeys=" + publicKeys +
                '}';
    }
}
