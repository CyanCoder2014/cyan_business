package com.cyancoder.taxpaysys.modules.tax_api.entity.general;

public enum Person {


    natural,
    legal,
    unknown;

    public static Person setValue(int value) {

        switch (value) {
            case 0:
                return Person.natural;
            case 1:
                return Person.legal;
            default:
                return Person.unknown;//Keep an default or error enum handy
        }
    }
}
