package com.example.identity.configuration;

import com.example.identity.dto.response.ApiResponse;
import com.example.identity.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/* Bắt lỗi ở Security (do lỗi ko xảy ra ở phía controller) */
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.error("Authentication error");
        /* Mục tiêu gửi response về với nội dung là template ApiResponse và kèm status code */
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // "application/ json"

        ApiResponse apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();     // đối tượng chuyển object thành string

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));  // đưa ApiResponse dạng String vào response
        response.flushBuffer();     // gửi response về
    }
}
