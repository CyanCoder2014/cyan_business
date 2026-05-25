package com.cyancoder.aiorchestrator.domain;

import java.util.ArrayList;
import java.util.List;

public class DeliveryBlueprint {
    private List<String> publicApis = new ArrayList<>();
    private List<String> botApis = new ArrayList<>();

    public List<String> getPublicApis() { return publicApis; }
    public void setPublicApis(List<String> publicApis) { this.publicApis = publicApis; }
    public List<String> getBotApis() { return botApis; }
    public void setBotApis(List<String> botApis) { this.botApis = botApis; }
}
