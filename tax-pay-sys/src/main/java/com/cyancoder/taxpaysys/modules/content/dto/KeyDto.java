package com.cyancoder.taxpaysys.modules.content.dto;

public class KeyDto {

    private String key;
    private String id;
    private String algorithm;
    private int purpose;

    public KeyDto(String key, String id, String algorithm, int purpose) {
        this.key = key;
        this.id = id;
        this.algorithm = algorithm;
        this.purpose = purpose;
    }

    public KeyDto(String key, String id, String algorithm) {
        this.key = key;
        this.id = id;
        this.algorithm = algorithm;
        this.purpose = 1;
    }

    public KeyDto(String key, String algorithm) {
        this.key = key;
        this.algorithm = algorithm;
        this.purpose = 1;
    }

    public KeyDto() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public int getPurpose() {
        return purpose;
    }

    public KeyDto setPurpose(int purpose) {
        this.purpose = purpose;
        return this;
    }

    @Override
    public String toString() {
        return "KeyDto{" +
                "key='" + key + '\'' +
                ", id='" + id + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", purpose=" + purpose +
                '}';
    }
}
