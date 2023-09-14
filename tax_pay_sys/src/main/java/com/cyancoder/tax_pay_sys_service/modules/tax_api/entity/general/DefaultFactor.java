package com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general;


public enum DefaultFactor {

    for_buyer,
    for_receiver,
    for_unknown;

    public static DefaultFactor setValue( int value){

        switch(value){
            case 0: return DefaultFactor.for_buyer;
            case 1: return DefaultFactor.for_receiver;
            default:
                return DefaultFactor.for_unknown ;//Keep an default or error enum handy
        }
    }
}
