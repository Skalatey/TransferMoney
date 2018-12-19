package com.trn.exception;

public class OverDraftException extends BusinessException {
    public OverDraftException(String message, String errorCode) {
        super(message, errorCode);
    }
}
