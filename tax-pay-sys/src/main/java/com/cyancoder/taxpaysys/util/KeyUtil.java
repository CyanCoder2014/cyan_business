package com.cyancoder.taxpaysys.util;

import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyUtil {

    private KeyUtil(){}


    private static  String CLIENT_ID_CYAN = "";
    private final static String PRIVATE_KEY_CYAN = "";
    private final static String PUBLIC_KEY_CYAN = "";


    public final static String SERVER_KEY = "1DVa47RvqiumpTNyK9HfFIdhgoupFkxT14XLDl65S55MF6HuQvo/RHSbBJ93FQ+2/x/Q2MNGB3BXOjNwM2pj3ojbDv3pj9CHzvaYQUYM1yOcFmIJqJ72uvVf9Jx9iTObaNNF6pl52ADmh85GTAH1hz+4pR/E9IAXUIl/YiUneYu0G4tiDY4ZXykYNknNfhSgxmn/gPHT+7kL31nyxgjiEEhK0B0vagWvdRCNJSNGWpLtlq4FlCWTAnPI5ctiFgq925e+sySjNaORCoHraBXNEwyiHT2hu5ZipIW2cCAwEAAQ==";




    private static final String ORG_PUBLIC_KEY = "";
    private static final String ORG_KEY_ID = "6a2bcd88-a871-4245-a393-2843eafe6e02";
//    private static  String CLIENT_ID = "A1116H";



    public static String getClientId(SellerUser seller){
        String clientKey = CLIENT_ID_CYAN;
        switch (seller){
            case cyan : clientKey = CLIENT_ID_CYAN;
                break;
        }
        return clientKey;

    }




    public static String getOrgPublicKey(){
        return ORG_PUBLIC_KEY;
    }

    public static String getOrgKeyId(){
        return ORG_KEY_ID;
    }


    public static String getStringPrivateKey(SellerUser seller){
        String privateKey = PRIVATE_KEY_CYAN;
        switch (seller){
            case cyan : privateKey = PRIVATE_KEY_CYAN;
                break;
        }
        privateKey = privateKey.replace("-----BEGIN PRIVATE KEY-----", "");
        privateKey = privateKey.replace("-----END PRIVATE KEY-----", "");
        privateKey = privateKey.replaceAll("\\s+","");
        return privateKey;
    }

    public static String getStringPublicKey(String seller){
        String privateKey = PUBLIC_KEY_CYAN;
        privateKey = privateKey.replace("-----BEGIN PUBLIC KEY-----", "");
        privateKey = privateKey.replace("-----END PUBLIC KEY-----", "");
        privateKey = privateKey.replaceAll("\\s+","");
        return privateKey;
    }
    public static PrivateKey getPrivateKey(SellerUser seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKey = PRIVATE_KEY_CYAN;
        switch (seller){
            case cyan : privateKey = PRIVATE_KEY_CYAN;
        }
        // Read in the key into a String
        StringBuilder pkcs8Lines = new StringBuilder();
        BufferedReader rdr = new BufferedReader(new StringReader(privateKey));
        String line;
        while ((line = rdr.readLine()) != null) {
            pkcs8Lines.append(line);
        }

        // Remove the "BEGIN" and "END" lines, as well as any whitespace
        String pkcs8Pem = pkcs8Lines.toString();
        pkcs8Pem = pkcs8Pem.replace("-----BEGIN PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replace("-----END PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replaceAll("\\s+","");

        // Base64 decode the result
        byte [] pkcs8EncodedBytes = Base64.getDecoder().decode(pkcs8Pem);

        // extract the private key
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);

        return privKey;
    }











    public static PublicKey getPublicKey(String serverKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        // Read in the key into a String
        StringBuilder pkcs8Lines = new StringBuilder();
        BufferedReader rdr = new BufferedReader(new StringReader(PUBLIC_KEY_CYAN));
        String line;
        while ((line = rdr.readLine()) != null) {
            pkcs8Lines.append(line);
        }

        // Remove the "BEGIN" and "END" lines, as well as any whitespace
        String pkcs8Pem = pkcs8Lines.toString();
        pkcs8Pem = pkcs8Pem.replace("-----BEGIN PUBLIC KEY-----", "");
        pkcs8Pem = pkcs8Pem.replace("-----END PUBLIC KEY-----", "");
        pkcs8Pem = pkcs8Pem.replaceAll("\\s+","");

        byte [] publicBytes = Base64.getDecoder().decode(pkcs8Pem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);


        return pubKey;
    }




    public static PublicKey getServerPublicKey(String serverKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        if(serverKey == null)
            serverKey = SERVER_KEY;

        byte [] publicBytes = Base64.getDecoder().decode(serverKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        return pubKey;
    }




}
