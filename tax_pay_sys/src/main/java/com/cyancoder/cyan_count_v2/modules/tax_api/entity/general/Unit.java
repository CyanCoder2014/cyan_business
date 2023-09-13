package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general;


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
