package com.cyancoder.taxpaysys.modules.transfer.exception;

public class TaxApiException extends RuntimeException {

    public TaxApiException() {
    }

    public TaxApiException(String message) {
        super(message);
    }

    public TaxApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaxApiException(Throwable cause) {
        super(cause);
    }

    public TaxApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
