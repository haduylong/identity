/* normalize response */
package com.example.identity_service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL) // exclude null field
public class ApiResponse<T> {
    @Builder.Default
    int code = 1000;    // default; success

    String message;     // trả về message khi có lỗi
    T result;
}
