package com.example.identity.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND(1001, "User not found", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1002, "Unauthenticated", HttpStatus.BAD_REQUEST),
    CAN_NOT_CREATE_TOKEN(1003, "Can not create token", HttpStatus.INTERNAL_SERVER_ERROR),
    CAN_NOT_VERIFY_TOKEN(1004, "Can not verify token", HttpStatus.BAD_REQUEST),
    INVALID_KEY_CONSTRAINT(1005, "Invalid key constraint", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_DOB(1006, "Date of birth must be at least {min}", HttpStatus.BAD_REQUEST)
    ;
    int code;
    String message;
    HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
