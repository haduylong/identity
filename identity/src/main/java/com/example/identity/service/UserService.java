package com.example.identity.service;

import com.example.identity.dto.request.user.UserCreateRequest;
import com.example.identity.dto.request.user.UserUpdateRequest;
import com.example.identity.dto.response.ApiResponse;
import com.example.identity.dto.response.user.UserResponse;
import com.example.identity.entity.Role;
import com.example.identity.entity.User;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.mapper.UserMapper;
import com.example.identity.repository.RoleRepository;
import com.example.identity.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;

    public UserResponse create(UserCreateRequest request) {
        // mapping
        User user = userMapper.toUser(request);

        // check exist
        if(userRepository.existsByUsername(user.getUsername()))
            throw new RuntimeException("User existed");

        List<Role> roleList =  roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roleList));

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user = userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    public List<UserResponse> getAll() {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        log.info("Username: {}", authentication.getName());
        log.info("Scope: {}", authentication.getAuthorities());

        List<User> userList = userRepository.findAll();

        return userList
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public void deleteById(String userId) {
        userRepository.deleteById(userId);
    }

    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        user = userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    public UserResponse getById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }
}
