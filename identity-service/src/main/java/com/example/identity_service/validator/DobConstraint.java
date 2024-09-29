package com.example.identity_service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

@Target({ElementType.FIELD})            // phạm vi áp dụng
@Retention(RetentionPolicy.RUNTIME)     // thời điểm xử lý
@Constraint(
        validatedBy = {DobValidator.class}  // class chịu trách nhiệm validate
)
public @interface DobConstraint {
    String message() default "Invalid date of birth";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int min();
}
