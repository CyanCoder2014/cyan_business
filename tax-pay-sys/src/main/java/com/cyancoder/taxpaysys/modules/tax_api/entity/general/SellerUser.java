package com.cyancoder.taxpaysys.modules.tax_api.entity.general;

public enum SellerUser {

    cyan;


    public static SellerUser getSellerById(int sellerId) {
        switch (sellerId) {
            case 1:
                return SellerUser.cyan;
            default:
                return null;

        }
    }




    }


