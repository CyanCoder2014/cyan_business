package com.cyancoder.taxpaysys.modules.tax_api.entity.general;

public enum SellerUser {

    cyan;


    public static SellerUser getSellerById(int sellerId) {
        if (sellerId == 1) {
            return SellerUser.cyan;
        }
        return null;
    }




    }


