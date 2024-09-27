package com.example.identity.controller;

import com.example.identity.dto.request.role.RoleRequest;
import com.example.identity.dto.response.ApiResponse;
import com.example.identity.dto.response.role.RoleResponse;
import com.example.identity.entity.Role;
import com.example.identity.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {
    RoleService roleService;

    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        RoleResponse roleResponse =roleService.create(request);

        return ApiResponse.<RoleResponse>builder()
                .result(roleResponse)
                .build();
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAll() {
        List<RoleResponse> responses = roleService.getAll();

        return ApiResponse.<List<RoleResponse>>builder()
                .result(responses)
                .build();
    }

    @DeleteMapping("/{role}")
    ApiResponse<Void> delete(@PathVariable String role) {
        roleService.delete(role);

        return ApiResponse.<Void>builder().build();
    }
}
