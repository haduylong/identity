package com.example.identity.controller;

import com.example.identity.dto.request.permission.PermissionRequest;
import com.example.identity.dto.response.ApiResponse;
import com.example.identity.dto.response.permission.PermissionResponse;
import com.example.identity.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {
    PermissionService permissionService;

    @PostMapping
    ApiResponse<PermissionResponse> create(@RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.create(request);

        return ApiResponse.<PermissionResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping
    ApiResponse<List<PermissionResponse>> getAll() {
        List<PermissionResponse> responses = permissionService.getAll();

        return ApiResponse.<List<PermissionResponse>>builder()
                .result(responses)
                .build();
    }

    @DeleteMapping("/{permission}")
    ApiResponse<Void> delete(@PathVariable String permission) {
        permissionService.delete(permission);

        return ApiResponse.<Void>builder()
                .build();
    }
}
