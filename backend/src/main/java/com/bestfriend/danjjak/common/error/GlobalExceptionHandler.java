package com.bestfriend.danjjak.common.error;

import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException exception) {
        return ResponseEntity.status(exception.getStatus())
                .header("Cache-Control", "no-store")
                .body(new ApiError(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        ConstraintViolationException.class,
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestPartException.class
    })
    public ResponseEntity<ApiError> handleValidationException(Exception exception) {
        if (exception instanceof HttpMessageNotReadableException unreadable
                && unreadable.getMostSpecificCause() instanceof ApiException apiException) {
            return handleApiException(apiException);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("Cache-Control", "no-store")
                .body(new ApiError("INVALID_REQUEST", "요청 값이 올바르지 않습니다."));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleUploadLimit(MaxUploadSizeExceededException exception) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .header("Cache-Control", "no-store")
                .body(new ApiError("AUDIO_TOO_LARGE", "녹음 파일은 10MB 이하로 올려 주세요."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Cache-Control", "no-store")
                .body(new ApiError("INTERNAL_ERROR", "처리하지 못했어요. 잠시 후 다시 시도해 주세요."));
    }
}
