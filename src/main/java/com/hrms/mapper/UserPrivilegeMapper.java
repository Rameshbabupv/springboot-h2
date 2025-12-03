package com.hrms.mapper;

import com.hrms.dto.request.OrganizationalScopeRequest;
import com.hrms.dto.request.UserPrivilegeRequest;
import com.hrms.dto.response.HrmsModuleResponse;
import com.hrms.dto.response.UserOrganizationalScopeResponse;
import com.hrms.dto.response.UserPrivilegeResponse;
import com.hrms.entity.HrmsModule;
import com.hrms.entity.UserOrganizationalScope;
import com.hrms.entity.UserPrivilege;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper for User Privilege entities and DTOs
 */
@Mapper(componentModel = "spring")
public interface UserPrivilegeMapper {

    // HrmsModule mappings
    HrmsModuleResponse toHrmsModuleResponse(HrmsModule entity);

    // UserPrivilege mappings
    UserPrivilegeResponse toUserPrivilegeResponse(UserPrivilege entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    UserPrivilege toUserPrivilege(UserPrivilegeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateUserPrivilegeFromRequest(UserPrivilegeRequest request, @MappingTarget UserPrivilege entity);

    // UserOrganizationalScope mappings
    UserOrganizationalScopeResponse toOrganizationalScopeResponse(UserOrganizationalScope entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    UserOrganizationalScope toOrganizationalScope(OrganizationalScopeRequest request);
}
