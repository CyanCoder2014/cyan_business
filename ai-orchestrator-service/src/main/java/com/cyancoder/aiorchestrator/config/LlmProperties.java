package com.cyancoder.aiorchestrator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "llm")
public class LlmProperties {
    private AiProvider provider = AiProvider.AUTO;
    private int maxParseAttempts = 3;
    private List<AiProvider> fallbackOrder = new ArrayList<>(List.of(AiProvider.OPENAI, AiProvider.GAPGPT, AiProvider.OPENROUTER, AiProvider.OLLAMA, AiProvider.HEURISTIC));
    private ProviderProperties openai = new ProviderProperties();
    private ProviderProperties openrouter = new ProviderProperties();
    private ProviderProperties gapgpt = new ProviderProperties();
    private ProviderProperties ollama = new ProviderProperties();

    public AiProvider getProvider() { return provider; }
    public void setProvider(AiProvider provider) { this.provider = provider; }
    public int getMaxParseAttempts() { return maxParseAttempts; }
    public void setMaxParseAttempts(int maxParseAttempts) { this.maxParseAttempts = maxParseAttempts; }
    public List<AiProvider> getFallbackOrder() { return fallbackOrder; }
    public void setFallbackOrder(List<AiProvider> fallbackOrder) { this.fallbackOrder = fallbackOrder; }
    public ProviderProperties getOpenai() { return openai; }
    public void setOpenai(ProviderProperties openai) { this.openai = openai; }
    public ProviderProperties getOpenrouter() { return openrouter; }
    public void setOpenrouter(ProviderProperties openrouter) { this.openrouter = openrouter; }
    public ProviderProperties getGapgpt() { return gapgpt; }
    public void setGapgpt(ProviderProperties gapgpt) { this.gapgpt = gapgpt; }
    public ProviderProperties getOllama() { return ollama; }
    public void setOllama(ProviderProperties ollama) { this.ollama = ollama; }

    public ProviderProperties getProviderProperties(AiProvider aiProvider) {
        Map<AiProvider, ProviderProperties> map = new EnumMap<>(AiProvider.class);
        map.put(AiProvider.OPENAI, openai);
        map.put(AiProvider.OPENROUTER, openrouter);
        map.put(AiProvider.GAPGPT, gapgpt);
        map.put(AiProvider.OLLAMA, ollama);
        return map.get(aiProvider);
    }

    public static class ProviderProperties {
        private String apiKey;
        private String baseUrl;
        private String model;
        private String completionsPath;
        private String referer;
        private String title;

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getCompletionsPath() { return completionsPath; }
        public void setCompletionsPath(String completionsPath) { this.completionsPath = completionsPath; }
        public String getReferer() { return referer; }
        public void setReferer(String referer) { this.referer = referer; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
}
