package com.cyancoder.aiorchestrator.domain;

public class BlueprintQuestionDefinition {
    private String key;
    private String prompt;
    private boolean required;

    public BlueprintQuestionDefinition() {
    }

    public BlueprintQuestionDefinition(String key, String prompt, boolean required) {
        this.key = key;
        this.prompt = prompt;
        this.required = required;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
}
