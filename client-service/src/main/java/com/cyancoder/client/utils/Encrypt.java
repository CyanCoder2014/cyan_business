package com.cyancoder.client.utils;

import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Encrypt {

    private static final String AES = "AES";
    private static final String PreSecret = "secret-E57ED#H#D5345T345AS";

    public static String hash(String code) {

        return new DigestUtils("SHA3-256").digestAsHex(code);

    }


    public static String encrypt(String code, String uniqueCode) {

        String SECRET =  PreSecret + uniqueCode;
        String res = "";
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(), AES);
            Cipher cipher = Cipher.getInstance(AES);

            cipher.init(Cipher.ENCRYPT_MODE, key);
            res = Base64.getEncoder().encodeToString(cipher.doFinal(code.getBytes()));

        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException |
                 NoSuchPaddingException e) {
            throw new IllegalStateException(e);
        }

        return res;
    }



    public static String decrypt(String code, String uniqueCode) {

        String SECRET = PreSecret + uniqueCode;
        String res = "";
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(), AES);
            Cipher cipher = Cipher.getInstance(AES);

            cipher.init(Cipher.DECRYPT_MODE, key);
            res = new String(cipher.doFinal(Base64.getDecoder().decode(code)));

        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException |
                 NoSuchPaddingException e) {
            throw new IllegalStateException(e);
        }

        return res;
    }







    }
