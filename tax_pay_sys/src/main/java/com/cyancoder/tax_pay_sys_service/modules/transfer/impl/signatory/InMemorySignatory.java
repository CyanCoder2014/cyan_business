package com.cyancoder.tax_pay_sys_service.modules.transfer.impl.signatory;

import com.cyancoder.tax_pay_sys_service.modules.transfer.exception.TaxApiException;
import com.cyancoder.tax_pay_sys_service.modules.transfer.interfaces.Signatory;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class InMemorySignatory implements Signatory {

    private PrivateKey privateKey;

    private String keyId;

    public InMemorySignatory(String base64PrivateKey, String keyId) {
        try {
            this.privateKey = getPrivateKeyFromBase64(base64PrivateKey);
        } catch (Exception e) {
            throw new TaxApiException("Invalid Private Key", e);
        }
        this.keyId = keyId;
    }

    @Override
    public String sign(String text) {
        try {
            byte[] data = text.getBytes("UTF8");
            Signature sig = Signature.getInstance("SHA256WITHRSA");
            sig.initSign(privateKey);
            sig.update(data);
            byte[] signatureBytes = sig.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception ex) {
            throw new TaxApiException("error in sign data", ex);
        }
    }

    @Override
    public String getKeyId() {
        return keyId;
    }

    public PrivateKey getPrivateKeyFromBase64(String base64PrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encoded = Base64.getDecoder().decode(base64PrivateKey);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }
}
