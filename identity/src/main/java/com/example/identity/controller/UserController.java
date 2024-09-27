package com.example.identity.controller;

import com.example.identity.dto.request.user.UserCreateRequest;
import com.example.identity.dto.request.user.UserUpdateRequest;
import com.example.identity.dto.response.ApiResponse;
import com.example.identity.dto.response.user.UserResponse;
import com.example.identity.entity.User;
import com.example.identity.mapper.UserMapper;
import com.example.identity.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> create(@RequestBody UserCreateRequest request) {
        UserResponse userResponse = userService.create(request);

        return ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getAll() {
        List<UserResponse> apiResponseList = userService.getAll();

        return ApiResponse.<List<UserResponse>>builder()
                .result(apiResponseList)
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<Void> delete(@PathVariable String userId) {
        userService.deleteById(userId);

        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> update(@PathVariable String userId ,@RequestBody UserUpdateRequest request) {
        UserResponse userResponse = userService.updateUser(userId, request);

        return ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
    }
}
