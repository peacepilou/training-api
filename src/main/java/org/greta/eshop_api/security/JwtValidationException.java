package org.greta.eshop_api.security;

public class JwtValidationException extends RuntimeException {

    public JwtValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
