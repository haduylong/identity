package com.example.identity.mapper;

import com.example.identity.dto.request.permission.PermissionRequest;
import com.example.identity.dto.response.permission.PermissionResponse;
import com.example.identity.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResponse toPermissionResponse(Permission permission);
    Permission toPermission(PermissionRequest request);
}
