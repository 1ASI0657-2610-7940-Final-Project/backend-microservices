package com.gigu.marketplace.application.exception;

public class SupabaseStorageException extends RuntimeException {
    public SupabaseStorageException(String message) {
        super(message);
    }

    public SupabaseStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
