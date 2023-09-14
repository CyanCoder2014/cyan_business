package com.cyancoder.tax_pay_sys_service.modules.tax_api.client.auth;


public class Token {

    private static Token tokenInstance = null;

    private Token(){}
    String token = null;

    public static synchronized Token getInstance()
    {
        if (tokenInstance == null)
            tokenInstance = new Token();

        return tokenInstance;
    }



    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
