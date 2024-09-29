package com.example.identity_service.service;

import com.example.identity_service.dto.request.UserCreationRequest;
import com.example.identity_service.dto.request.UserUpdateRequest;
import com.example.identity_service.dto.response.UserResponse;
import com.example.identity_service.entity.User;
import com.example.identity_service.enums.Role;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.mapper.UserMapper;
import com.example.identity_service.repository.RoleRepository;
import com.example.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // tự khởi tạo constructor và inject; bean cần là final; thay vì phải sử dụng @Autowire với các bean
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest dto) {
        // Kiểm tra tồn tại
        if(userRepository.existsByUsername(dto.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        // mapping giữa dto và entity
        User user = userMapper.toUser(dto);
        // encode password
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // mặc định có quyền user
        com.example.identity_service.entity.Role role = roleRepository.findById(Role.USER.name())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Set<com.example.identity_service.entity.Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")   // prefix SCOPE_ được tự thêm trong SecurityConfig; prefix ROLE_ được thêm trong updateUser
    public List<UserResponse> getUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map((user) -> userMapper.toUserResponse(user))
                .collect(Collectors.toUnmodifiableList());
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUserById(String userId) {
        return userMapper.toUserResponse(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)); // lấy ra user có userId
        userMapper.updateUser(user, request);                                   // mapping đưa request thành user
        user.setPassword(passwordEncoder.encode(request.getPassword()));        // đặt password mới

        var roles = roleRepository.findAllById(request.getRoles());             // tìm tất cả các role có trong request
        user.setRoles(new HashSet<>(roles));                                    // đặt role mới

        return userMapper.toUserResponse(userRepository.save(user));            // lưu và mapping sang response
    }

    public void deleteUser(String userId){
        userRepository.deleteById(userId);
    }

    /* lấy thông tin của chính mình */
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();

        String username = context.getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                                new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }
}
