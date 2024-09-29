package com.example.identity_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class DobValidator implements ConstraintValidator<DobConstraint, LocalDate> {
    private int min;
    /* lấy các dữ liệu của annotation */
    @Override
    public void initialize(DobConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        min = constraintAnnotation.min();   // lấy giá trị của field min của annotation
    }

    /* validate */
    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        if(Objects.isNull(localDate))
            return true;

        long years = ChronoUnit.YEARS.between(localDate, LocalDate.now());  // khoảng cách giữa dob và thời gian hiện tại

        return years >= min;
    }
}
