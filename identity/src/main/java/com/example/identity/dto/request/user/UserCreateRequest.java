package com.example.identity.dto.request.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {
    String username;
    String password;
    String firstName;
    String lastName;
    LocalDate dob;
    Set<String> roles;
}
