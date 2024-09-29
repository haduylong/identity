package com.example.identity_service.exception;

import com.example.identity_service.dto.request.ApiResponse;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handleRuntimeException(Exception exception){
        ApiResponse resp = new ApiResponse();
        resp.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        resp.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest().body(resp);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException exception){
        ErrorCode errorCode = exception.getErrorCode();

        ApiResponse resp = new ApiResponse();
        resp.setCode(errorCode.getCode());
        resp.setMessage(errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatusCode())
                .body(resp);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException exception){
        ApiResponse resp = new ApiResponse();

        resp.setCode(ErrorCode.UNAUTHORIZED.getCode());
        resp.setMessage(ErrorCode.UNAUTHORIZED.getMessage());

        return ResponseEntity
                .status(ErrorCode.UNAUTHORIZED.getHttpStatusCode())
                .body(resp);
    }

    /* các lỗi có giá trị message của annotation trùng với key của Role enum */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception){
        String errKey = exception.getFieldError().getDefaultMessage();  // in this case: message of annotation
        ErrorCode errorCode = ErrorCode.INVALID_KEY;    // trường hợp key bị sai

        Map<String, Object> attributes = null;

        try {
            errorCode = ErrorCode.valueOf(errKey);

            var constraintViolation = exception.getBindingResult()
                    .getAllErrors()
                    .getFirst()
                    .unwrap(ConstraintViolation.class);

            attributes = constraintViolation.getConstraintDescriptor()
                                        .getAttributes();

        } catch (IllegalArgumentException e) {

        }


        ApiResponse resp = new ApiResponse();
        resp.setCode(errorCode.getCode());
        resp.setMessage(Objects.nonNull(attributes) ?
                mapAttribute(errorCode.getMessage(), attributes)    // not null chọn lỗi trong attributes
                : errorCode.getMessage()                            // null chọn lỗi INVALID_KEY
        );

        return ResponseEntity.badRequest().body(resp);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));

        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}
