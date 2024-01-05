package com.cyancoder.taxpaysys.modules.content.dto;

import com.cyancoder.taxpaysys.modules.content.enumeration.OperatorType;

public class FilterDto {
    private String field;
    private String value;
    private OperatorType operator;
    private boolean or;

    public FilterDto() {
        this.or = false;
        this.value = "";
    }

    public FilterDto(String field, OperatorType operator, String value) {
        this(field, operator);
        this.value = value;
    }

    public FilterDto(String field, OperatorType operator, String value, boolean or) {
        this(field, operator, value);
        this.or = or;
    }

    public FilterDto(String field, OperatorType operator) {
        this();
        this.field = field;
        this.operator = operator;
    }

    public String getField() {
        return this.field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public OperatorType getOperator() {
        return this.operator;
    }

    public void setOperator(OperatorType operator) {
        this.operator = operator;
    }

    public boolean isOr() {
        return this.or;
    }

    public void setOr(boolean or) {
        this.or = or;
    }
}
