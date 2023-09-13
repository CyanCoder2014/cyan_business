package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general;

public enum SellerType {

    includeTaxesSystem,
    include81Article,
    notIncludeTaxesSystem,
    unknown;

    public static SellerType setValue(int value) {

        switch (value) {
            case 0:
                return SellerType.includeTaxesSystem;
            case 1:
                return SellerType.include81Article;
            case 2:
                return SellerType.notIncludeTaxesSystem;
            default:
                return SellerType.unknown;//Keep an default or error enum handy
        }
    }
}
