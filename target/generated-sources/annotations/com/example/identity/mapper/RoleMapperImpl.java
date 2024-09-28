package com.example.identity.mapper;

import com.example.identity.dto.request.role.RoleRequest;
import com.example.identity.dto.response.role.RoleResponse;
import com.example.identity.entity.Permission;
import com.example.identity.entity.Role;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-28T10:48:29+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21 (Oracle Corporation)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public Role toRole(RoleRequest request) {
        if ( request == null ) {
            return null;
        }

        Role.RoleBuilder role = Role.builder();

        role.name( request.getName() );
        role.description( request.getDescription() );

        return role.build();
    }

    @Override
    public RoleResponse toRoleResponse(Role role) {
        if ( role == null ) {
            return null;
        }

        RoleResponse.RoleResponseBuilder roleResponse = RoleResponse.builder();

        roleResponse.name( role.getName() );
        roleResponse.description( role.getDescription() );
        Set<Permission> set = role.getPermissions();
        if ( set != null ) {
            roleResponse.permissions( new LinkedHashSet<Permission>( set ) );
        }

        return roleResponse.build();
    }
}
