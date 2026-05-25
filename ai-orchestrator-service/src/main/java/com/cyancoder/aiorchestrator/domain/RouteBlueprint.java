package com.cyancoder.aiorchestrator.domain;

public class RouteBlueprint {
    private String routeKey;
    private String path;
    private String targetServiceKey;
    private String targetEntityKey;
    private String targetRecordKey;
    private String themeRecordKey;
    private String pageType;

    public String getRouteKey() { return routeKey; }
    public void setRouteKey(String routeKey) { this.routeKey = routeKey; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getTargetServiceKey() { return targetServiceKey; }
    public void setTargetServiceKey(String targetServiceKey) { this.targetServiceKey = targetServiceKey; }
    public String getTargetEntityKey() { return targetEntityKey; }
    public void setTargetEntityKey(String targetEntityKey) { this.targetEntityKey = targetEntityKey; }
    public String getTargetRecordKey() { return targetRecordKey; }
    public void setTargetRecordKey(String targetRecordKey) { this.targetRecordKey = targetRecordKey; }
    public String getThemeRecordKey() { return themeRecordKey; }
    public void setThemeRecordKey(String themeRecordKey) { this.themeRecordKey = themeRecordKey; }
    public String getPageType() { return pageType; }
    public void setPageType(String pageType) { this.pageType = pageType; }
}

