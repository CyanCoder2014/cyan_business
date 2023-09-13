package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general;


public enum BillState {

    bill_state_1,
    bill_state_2,
    bill_state_3,
    other;

    public static BillState setValue(int value) {

        switch (value) {

            case 0:
                return BillState.bill_state_1;
            case 1:
                return BillState.bill_state_2;
            case 2:
                return BillState.bill_state_3;

            default:
                return BillState.other;//Keep an default or error enum handy
        }
    }
}
