package com.cyancoder.taxpaysys.modules.tax_api.entity.general;


public enum EditState {

    edited,
    no_edited,
    unknown;

    public static EditState setValue( int value){

        switch(value){
            case 0: return EditState.no_edited;
            case 1: return EditState.edited;
            default:
                return EditState.unknown ;//Keep an default or error enum handy
        }
    }
}
