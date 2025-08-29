package com.tlcn.sportsnet_backend.payload.exception;

import org.springframework.security.core.AuthenticationException;

public class CustomUnauthorizedException extends AuthenticationException  {
    public CustomUnauthorizedException(String msg) {
        super(msg);
    }
}
