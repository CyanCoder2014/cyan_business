package com.cyancoder.tax_pay_sys_service.modules.tax_api.entity;



public enum DeliveryType {
    with_tractor,
    with_out_tractor,
    delivery_single,
    delivery_double,
    delivery_tractor,
    unknown;

    public static DeliveryType setValue(int value){

        switch(value){
            case 0: return DeliveryType.with_out_tractor;
            case 1: return DeliveryType.with_tractor;
            default:
                return DeliveryType.unknown ;//Keep an default or error enum handy
        }
    }
}
