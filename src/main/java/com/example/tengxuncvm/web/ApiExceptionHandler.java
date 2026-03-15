package com.example.tengxuncvm.web;

import com.example.tengxuncvm.service.PriceQueryException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PriceQueryException.class)
    public ResponseEntity<ApiErrorResponse> handlePriceQuery(PriceQueryException ex) {
        ApiErrorResponse response = new ApiErrorResponse();
        response.setCode(ex.getCode());
        response.setMessage(ex.getMessage());
        response.setRegion(ex.getRegion());
        response.setZone(ex.getZone());
        response.setInstanceType(ex.getInstanceType());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex) {
        ApiErrorResponse response = new ApiErrorResponse();
        response.setCode("OPERATION_FAILED");
        response.setMessage(ex.getMessage() == null ? "Operation failed." : ex.getMessage());

        Throwable cause = ex.getCause();
        if (cause instanceof TencentCloudSDKException) {
            response.setCode("TENCENT_CLOUD_ERROR");
            response.setMessage(cause.getMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
