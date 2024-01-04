package com.cyancoder.taxpaysys.modules.transfer.impl.encrypter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cyancoder.taxpaysys.modules.transfer.dto.PacketDto;
import com.cyancoder.taxpaysys.modules.transfer.exception.TaxApiException;
import com.cyancoder.taxpaysys.modules.transfer.interfaces.Encrypter;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class DefaultEncrypter implements Encrypter {

    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private final String encryptionKeyId;
    private final PublicKey taxOrgPublicKey;

    private static final ObjectMapper mapper = new ObjectMapper();

    public DefaultEncrypter(String taxOrgPublicKey, String encryptionKeyId) {
        this.encryptionKeyId = encryptionKeyId;
        this.taxOrgPublicKey = getPublicKeyFromBase64(taxOrgPublicKey);
    }

    @Override
    public void encrypt(List<PacketDto> packets) {
        try {
            SecretKey aesKey = generateAesSecretKey();
            byte[] iv = generateIV();
            String hexIv = hex(iv);

            String rsaEncryptSymmetricKey = encryptSymmetricKey(
                    hex(aesKey.getEncoded()),
                    taxOrgPublicKey);

            for (PacketDto packetDto : packets) {
                packetDto.setIv(hexIv);
                packetDto.setEncryptionKeyId(encryptionKeyId);
                packetDto.setSymmetricKey(rsaEncryptSymmetricKey);
                packetDto.setData(doEncryption(aesKey, iv, packetDto.getData()));
            }
        } catch (Exception ex) {
            throw new TaxApiException("error in encrypt packets", ex);
        }
    }

    private String doEncryption(SecretKey aesKey, byte[] iv, Object data) throws Exception {

        byte[] textData;
        if (!(data instanceof String)) {
            textData = mapper.writeValueAsBytes(data);
        } else {
            textData = ((String) data).getBytes();
        }

        byte[] pText = xor(textData, aesKey.getEncoded());
        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

        byte[] encryptByte = cipher.doFinal(pText);
        return Base64.getEncoder().encodeToString(encryptByte);
    }

    private String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private byte[] generateIV() {
        return getRandomNonce(16);
    }

    private byte[] getRandomNonce(int byteSize) {
        byte[] nonce = new byte[byteSize];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    private SecretKey generateAesSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, SecureRandom.getInstanceStrong());
        return keyGen.generateKey();
    }

    private String encryptSymmetricKey(String symmetricKeyJson, PublicKey publicKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        OAEPParameterSpec oaepParameterSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        Cipher encryptCipher = Cipher.getInstance("RSA/ECB/OAEPPadding");

        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParameterSpec);

        byte[] secretMessageBytes = symmetricKeyJson.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);

        return Base64.getEncoder().encodeToString(encryptedMessageBytes);
    }


    private byte[] xor(byte[] b1, byte[] b2) {

        byte[] oneAndTwo;
        if (b1.length < b2.length) {
            oneAndTwo = xorBlocks(b1, b2);
        } else {
            oneAndTwo = xorBlocks(b2, b1);
        }

        int length = oneAndTwo.length;
        while (length > 0 && oneAndTwo[length - 1] == 0)
            length--;
        if (length < oneAndTwo.length)
            return Arrays.copyOf(oneAndTwo, length);
        return oneAndTwo;
    }

    private byte[] xorBlocks(byte[] smallerArray, byte[] biggerArray) {

        byte[] oneAndTwo = new byte[biggerArray.length];

        int blockSize = (int) Math.ceil((double) biggerArray.length / smallerArray.length);
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < smallerArray.length; j++) {

                if ((i * smallerArray.length) + j >= biggerArray.length) {
                    break;
                }

                oneAndTwo[(i * smallerArray.length) + j] = (byte) (smallerArray[j] ^ biggerArray[(i * smallerArray.length) + j]);
            }
        }

        return oneAndTwo;
    }

    private PublicKey getPublicKeyFromBase64(String key) {
        try {
            byte[] byteKey = Base64.getDecoder().decode(key);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePublic(X509publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
