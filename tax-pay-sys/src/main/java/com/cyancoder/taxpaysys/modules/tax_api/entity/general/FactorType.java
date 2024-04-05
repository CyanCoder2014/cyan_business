package com.cyancoder.taxpaysys.modules.tax_api.entity.general;


public enum FactorType {

    for_buyer,
    for_receiver,
    for_default,
    for_unknown;

    public static FactorType setValue(int value) {

        switch (value) {
            case 0:
                return FactorType.for_buyer;
            case 1:
                return FactorType.for_receiver;
            case 2:
                return FactorType.for_default;
            default:
                return FactorType.for_unknown;//Keep an default or error enum handy
        }
    }
}
