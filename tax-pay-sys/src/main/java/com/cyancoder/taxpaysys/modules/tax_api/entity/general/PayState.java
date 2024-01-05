package com.cyancoder.taxpaysys.modules.tax_api.entity.general;


public enum PayState {

    in_fund,
    collecting,
    collected,
    cash,
    credit,
    disable,
    thrust,
    unknown;

    public static PayState setValue(int value) {

        switch (value) {
            case 0:
                return PayState.in_fund;
            case 1:
                return PayState.collecting;
            case 2:
                return PayState.collected;
            case 3:
                return PayState.cash;
            case 4:
                return PayState.credit;
            case 5:
                return PayState.disable;
            default:
                return PayState.unknown;//Keep an default or error enum handy
        }
    }

    public static PayState setValue(FactorState factorState) {

        switch (factorState) {
            case cash:
                return PayState.cash;

            case credit:
                return PayState.credit;

            default:
                return PayState.unknown;//Keep an default or error enum handy
        }

    }
}
