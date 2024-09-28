package com.example.identity.controller;

import com.example.identity.dto.request.auth.AuthenticationRequest;
import com.example.identity.dto.request.auth.IntrospectRequest;
import com.example.identity.dto.request.auth.LogoutRequest;
import com.example.identity.dto.request.auth.RefreshRequest;
import com.example.identity.dto.response.ApiResponse;
import com.example.identity.dto.response.auth.AuthenticationResponse;
import com.example.identity.dto.response.auth.IntrospectResponse;
import com.example.identity.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = authenticationService.authenticate(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(response)
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        IntrospectResponse introspectResponse = authenticationService.introspect(request);

        return ApiResponse.<IntrospectResponse>builder()
                .result(introspectResponse)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<String> logout(@RequestBody LogoutRequest request) throws ParseException {
        authenticationService.logout(request);

        return ApiResponse.<String>builder()
                .result("Logout successfully")
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) throws ParseException {
        AuthenticationResponse response = authenticationService.refresh(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(response)
                .build();
    }
}
