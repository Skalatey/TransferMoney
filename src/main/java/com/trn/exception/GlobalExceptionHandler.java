package com.trn.exception;

import com.trn.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity handleBusinessException(HttpServletRequest req, Exception e){
        BusinessException businessEx = (BusinessException) e;

        String errorMsg = (e.getMessage() == null) ? e.getClass().getSimpleName() : e.getMessage();

        ErrorResponse response = new ErrorResponse();
        response.setErrorCode(businessEx.getErrorCode());
        response.setErrorMessage(errorMsg);

        return ResponseEntity.status(businessEx.getHttpStatus()).body(response);
    }
}
