package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.api.dto.AiProviderProfileContracts.ProfileView;
import com.cyancoder.aiorchestrator.api.dto.AiProviderProfileContracts.SaveProfileRequest;
import com.cyancoder.aiorchestrator.domain.AiProviderProfile;
import com.cyancoder.aiorchestrator.repo.AiProviderProfileRepository;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiProviderProfileService {
    private static final Set<String> MODALITIES=Set.of("TEXT","IMAGE","AUDIO","VIDEO","FILE");
    private final AiProviderProfileRepository repository; private final AiSecretReferenceResolver secrets;
    public AiProviderProfileService(AiProviderProfileRepository repository,AiSecretReferenceResolver secrets){this.repository=repository;this.secrets=secrets;}
    public List<ProfileView> list(String tenant,String site){requiredTenant(tenant);return repository.findByTenantKeyAndSiteKeyOrderByDisplayNameAsc(tenant,blank(site)).stream().map(this::view).toList();}
    public ProfileView save(String tenant,String site,String actor,SaveProfileRequest request){requiredTenant(tenant);validateUrl(request.baseUrl());validatePath(request.operationPath());Set<String> modalities=request.modalities().stream().map(v->v.toUpperCase(Locale.ROOT)).collect(java.util.stream.Collectors.toSet());if(!MODALITIES.containsAll(modalities))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported modality");AiProviderProfile profile=repository.findByTenantKeyAndSiteKeyAndProfileKey(tenant,blank(site),request.profileKey()).orElseGet(AiProviderProfile::new);Instant now=Instant.now();if(profile.getId()==null){profile.setId(tenant+"|"+(blank(site)==null?"_tenant":site)+"|"+request.profileKey());profile.setTenantKey(tenant);profile.setSiteKey(blank(site));profile.setProfileKey(request.profileKey());profile.setCreatedAt(now);profile.setCreatedBy(actor);}profile.setDisplayName(request.displayName().trim());profile.setBaseUrl(stripTrailingSlash(request.baseUrl().trim()));profile.setOperationPath(request.operationPath().trim());profile.setModel(request.model().trim());profile.setSecretRef(request.secretRef().trim());profile.setModalities(modalities);profile.setEnabled(request.enabled());profile.setUpdatedAt(now);return view(repository.save(profile));}
    public AiProviderProfile require(String tenant,String site,String key){AiProviderProfile p=repository.findByTenantKeyAndSiteKeyAndProfileKey(tenant,blank(site),key).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"AI provider profile not found"));if(!p.isEnabled())throw new ResponseStatusException(HttpStatus.CONFLICT,"AI_PROVIDER_DISABLED");if(secrets.resolve(p.getSecretRef())==null)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"AI_PROVIDER_NOT_CONFIGURED");return p;}
    public String secret(AiProviderProfile profile){String value=secrets.resolve(profile.getSecretRef());if(value==null)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"AI_PROVIDER_NOT_CONFIGURED");return value;}
    private ProfileView view(AiProviderProfile p){return new ProfileView(p.getProfileKey(),p.getDisplayName(),p.getBaseUrl(),p.getOperationPath(),p.getModel(),p.getSecretRef(),Set.copyOf(p.getModalities()),p.isEnabled(),!p.isEnabled()?"DISABLED":secrets.resolve(p.getSecretRef())==null?"NOT_CONFIGURED":"CONFIGURED",p.getRevision(),p.getUpdatedAt());}
    private void validateUrl(String value){try{URI uri=URI.create(value);if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null||uri.getFragment()!=null)throw new IllegalArgumentException();}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"baseUrl must be an absolute HTTPS URL without credentials or fragment");}}
    private void validatePath(String value){if(!value.startsWith("/")||value.contains("..")||value.contains("?")||value.contains("#"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"operationPath must be a bounded absolute path");}
    private void requiredTenant(String v){if(v==null||v.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"X-Tenant-Key is required");}
    private String blank(String v){return v==null||v.isBlank()?null:v.trim();}private String stripTrailingSlash(String v){return v.endsWith("/")?v.substring(0,v.length()-1):v;}
}
