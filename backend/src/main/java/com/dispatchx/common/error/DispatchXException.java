package com.dispatchx.common.error;

import org.springframework.http.HttpStatus;

/**
 * Base application exception carrying a frozen ErrorCode and HttpStatus.
 */
public class DispatchXException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus status;

    public DispatchXException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = errorCode.getDefaultStatus();
    }

    public DispatchXException(ErrorCode errorCode, HttpStatus status, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public DispatchXException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = errorCode.getDefaultStatus();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
