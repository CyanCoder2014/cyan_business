package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.ConnectorCredential;
import com.cyancoder.automationorchestrator.repo.ConnectorCredentialRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
public class ConnectorCredentialService {
    private final ConnectorCredentialRepository repository;
    private final byte[] key;
    public ConnectorCredentialService(ConnectorCredentialRepository repository, @Value("${automation.credentials.master-key:local-development-automation-key}") String masterKey) {
        this.repository = repository;
        try { this.key = MessageDigest.getInstance("SHA-256").digest(masterKey.getBytes(StandardCharsets.UTF_8)); }
        catch (Exception ex) { throw new IllegalStateException(ex); }
    }
    public ConnectorCredential save(String tenant, String site, ConnectorCredential value) {
        value.setTenantKey(scope(tenant)); value.setSiteKey(scope(site));
        if (value.getEncryptedSecret() == null || value.getEncryptedSecret().isBlank()) throw new IllegalArgumentException("credential secret is required");
        value.setEncryptedSecret(encrypt(value.getEncryptedSecret())); value.setUpdatedAt(Instant.now());
        return repository.save(value);
    }
    public List<ConnectorCredential> list(String tenant, String site) { return repository.findAllByTenantKeyAndSiteKeyOrderByNameAsc(scope(tenant), scope(site)); }
    public ConnectorCredential active(String tenant, String site, String id) { return repository.findFirstByIdAndTenantKeyAndSiteKeyAndActiveTrue(id, scope(tenant), scope(site)).orElseThrow(); }
    public ConnectorCredential rotate(String tenant,String site,String id,String secret){ConnectorCredential value=active(tenant,site,id);value.setEncryptedSecret(secret);return save(tenant,site,value);}
    public String secret(ConnectorCredential value) { return decrypt(value.getEncryptedSecret()); }
    private String encrypt(String clear) {
        try { byte[] iv = new byte[12]; new SecureRandom().nextBytes(iv); Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv)); byte[] encrypted = cipher.doFinal(clear.getBytes(StandardCharsets.UTF_8)); byte[] joined = new byte[iv.length + encrypted.length]; System.arraycopy(iv,0,joined,0,iv.length); System.arraycopy(encrypted,0,joined,iv.length,encrypted.length); return Base64.getEncoder().encodeToString(joined); }
        catch (Exception ex) { throw new IllegalStateException("failed to encrypt connector credential", ex); }
    }
    private String decrypt(String encoded) {
        try { byte[] joined = Base64.getDecoder().decode(encoded); byte[] iv = java.util.Arrays.copyOfRange(joined,0,12); byte[] encrypted = java.util.Arrays.copyOfRange(joined,12,joined.length); Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv)); return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8); }
        catch (Exception ex) { throw new IllegalStateException("failed to decrypt connector credential", ex); }
    }
    private String scope(String value) { return value == null || value.isBlank() ? "default" : value.trim(); }
}
