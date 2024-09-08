//package com.cyancoder.taxpaysys.modules.tax_api.service;
//
//import com.cyancoder.taxpaysys.util.Encrypt;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//import org.jasypt.util.text.AES256TextEncryptor;
//
//import java.util.concurrent.TimeUnit;
//
//@Service
//public class TokenManagerService {
//
//    @Autowired
//    private StringRedisTemplate redisTemplate;
//
//
//    public String getToken(String key) {
//        String encryptedToken = redisTemplate.opsForValue().get(key);
//
//        if (encryptedToken != null) {
//            String decryptedToken = Encrypt.decrypt(encryptedToken,key);
//            return decryptedToken;
//        }
//
//        String newToken = requestNewToken();
//        String encryptedNewToken = Encrypt.encrypt(newToken,key);
//        redisTemplate.opsForValue().set(key, encryptedNewToken, 60, TimeUnit.SECONDS);
//
//        return newToken;
//    }
//
//    private String requestNewToken() {
//        // todo: Implement your logic to request a new token (e.g., from an external service)
//        return "newGeneratedToken";
//    }
//}
//
