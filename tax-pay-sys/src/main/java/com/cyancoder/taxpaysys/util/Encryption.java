package com.cyancoder.taxpaysys.util;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.security.*;


public class Encryption {

    private Encryption(){}

    public static SecretKey getAESKey(int keysize) throws
            NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(keysize, SecureRandom.getInstanceStrong());
        return keyGen.generateKey();
    }



    public static byte[] getRandomNonce(int byteSize) {
        byte[] nonce = new byte[byteSize];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }



    public static byte[] encrypt(byte[] pText, SecretKey secret, byte[] iv)
            throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secret, new GCMParameterSpec(128, iv));
        return cipher.doFinal(pText);
    }


    public static byte[] xor(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        int min = 0;
        int size = aLen > bLen ? aLen : bLen;
        byte[] c = new byte[size];
        if (size == aLen) {
            min = bLen;
            System.arraycopy(a, min, c, min, size - min);
        } else {
            min = aLen;
            System.arraycopy(b, min, c, min, size - min);
        }
        for (int i = 0; i < min; i++) {
            c[i] = (byte) (a[i] ^ b[i]);
        }
        return c;
    }











    // RSA-OAEP-SHA256
    public static String encrypt(String text, PublicKey publicKey) throws
            NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encryptCipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
                encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] secretMessageBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
        return Base64.getEncoder().encodeToString(encryptedMessageBytes);
    }
}
