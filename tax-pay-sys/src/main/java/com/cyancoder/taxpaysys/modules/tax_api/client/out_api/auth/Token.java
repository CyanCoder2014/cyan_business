package com.cyancoder.taxpaysys.modules.tax_api.client.out_api.auth;


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
