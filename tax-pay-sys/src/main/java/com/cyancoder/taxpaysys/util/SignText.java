package com.cyancoder.taxpaysys.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class SignText {

    private SignText(){}

    public static String getSignedText(String text, String algorithm, PrivateKey
            privateKey) throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        Signature sig = Signature.getInstance(algorithm == null ? " SHA256WITHRSA"
                : algorithm);
        sig.initSign(privateKey);
        sig.update(data);
        byte[] signatureBytes = sig.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

}
