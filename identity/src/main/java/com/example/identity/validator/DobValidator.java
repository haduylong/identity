package com.example.identity.validator;

import com.example.identity.exception.ErrorCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class DobValidator implements ConstraintValidator<DobConstraint, LocalDate> {
    private int min;
    private String message;
    @Override
    public void initialize(DobConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        min = constraintAnnotation.min();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if(Objects.isNull(value))
            return true;

        long years = ChronoUnit.YEARS.between(value, LocalDate.now());
        if(years <= min){
            ErrorCode errorCode = ErrorCode.INVALID_DOB;
            // Replace {min} placeholder with actual value of min
            String newMessage = message + "::" + errorCode.getMessage().replace("{min}", String.valueOf(min));

            // Disable default constraint violation and set custom message
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(newMessage)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
