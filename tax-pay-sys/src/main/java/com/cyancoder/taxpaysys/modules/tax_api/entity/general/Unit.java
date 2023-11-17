package com.cyancoder.taxpaysys.modules.tax_api.entity.general;


public enum Unit {

    kilo("کیلوگرم"),
    tonne("تن"),
    niches("سوله"),
    silo("سیلو"),
    litre("لیتر"),
    kilowatt("کیلووات"),
    kilowatt_litre("کیلووات لیتر"),
    unknown("نامشخص");

    public final String label;

    private Unit(String label) {
        this.label = label;
    }
}
