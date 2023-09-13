package main.java.com.cyancoder.tax_pay_sys_service.modules.content.enumeration;

public enum OperatorType {
    EQ,
    NE,
    GE,
    BETWEEN,
    GT,
    ILIKE,
    LE,
    LT,
    LIKE,
    IN,
    IS_NULL,
    IS_NOT_NULL,
    PATTERN;

    private OperatorType() {
    }
}
