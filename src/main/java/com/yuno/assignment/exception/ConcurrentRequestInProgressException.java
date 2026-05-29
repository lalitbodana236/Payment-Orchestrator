package com.yuno.assignment.exception;

public class ConcurrentRequestInProgressException extends RuntimeException {

    public ConcurrentRequestInProgressException(String idempotencyKey) {
        super("A request with the same idempotency key is currently being processed: " + idempotencyKey);
    }
}
