package com.example.identity_service.configuration;

import com.example.identity_service.dto.request.ApiResponse;
import com.example.identity_service.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/* Hỗ trợ bắt lỗi UNAUTHENTICATION; vì lỗi này xảy ra trước khi vào controller*/
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        /* gửi response về */
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        response.setStatus(errorCode.getHttpStatusCode().value());  // response status code
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);  // content là kiểu json

        ApiResponse<?> apiResponse = ApiResponse.builder()          // tạo ApiResponse
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();     // đối tượng chuyển object thành string

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));   // đưa ApiResponse dạng String vào response
        response.flushBuffer();                             // gửi đi
    }
}
