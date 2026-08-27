package com.cyancoder.dynamiccore.model;

/**
 * Configuration for a field of type "relation": the stored value is the target record's recordKey,
 * while forms render a searchable picker showing {@code displayField} from the target record.
 *
 * <p>{@code publicLookup} is an explicit, per-field opt-in that lets anonymous public forms search
 * this relation. It defaults to false so publishing a public form never silently exposes a
 * searchable read API over an entity that was not deliberately made public.
 */
public class RelationDefinition {
    private String serviceKey;
    private String entityKey;
    private String displayField;
    private boolean publicLookup;

    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }
    public String getEntityKey() { return entityKey; }
    public void setEntityKey(String entityKey) { this.entityKey = entityKey; }
    public String getDisplayField() { return displayField; }
    public void setDisplayField(String displayField) { this.displayField = displayField; }
    public boolean isPublicLookup() { return publicLookup; }
    public void setPublicLookup(boolean publicLookup) { this.publicLookup = publicLookup; }
}
