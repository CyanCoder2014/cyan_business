package com.cyancoder.automationorchestrator.service;

import java.util.List;
import java.util.Map;

final class AutomationSchemaSupport {
    private AutomationSchemaSupport() { }
    static void validate(Map<String,Object> schema,Map<String,Object> value,String label){
        if(schema==null||schema.isEmpty())return;
        for(Object required:AutomationDataSupport.list(schema.get("required")))if(!value.containsKey(required.toString())||value.get(required.toString())==null)throw new IllegalArgumentException(label+" is missing required property: "+required);
        Map<String,Object> properties=AutomationDataSupport.map(schema.get("properties"));
        properties.forEach((key,definition)->{if(!value.containsKey(key)||value.get(key)==null)return;String type=AutomationDataSupport.string(AutomationDataSupport.map(definition).get("type"));if(type!=null&&!matches(type,value.get(key)))throw new IllegalArgumentException(label+" property "+key+" must be "+type);});
        if(Boolean.FALSE.equals(schema.get("additionalProperties")))for(String key:value.keySet())if(!properties.containsKey(key))throw new IllegalArgumentException(label+" contains unsupported property: "+key);
    }
    private static boolean matches(String type,Object value){return switch(type){case"object"->value instanceof Map<?,?>;case"array"->value instanceof List<?>;case"string"->value instanceof String;case"number"->value instanceof Number;case"integer"->value instanceof Byte||value instanceof Short||value instanceof Integer||value instanceof Long;case"boolean"->value instanceof Boolean;case"null"->value==null;default->true;};}
}
