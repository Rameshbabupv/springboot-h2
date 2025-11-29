package com.hrms.mapper;

import com.hrms.dto.request.UserAccountRequest;
import com.hrms.dto.request.UserAccountUpdateRequest;
import com.hrms.dto.response.UserAccountResponse;
import com.hrms.dto.response.UserActivityLogResponse;
import com.hrms.dto.response.UserSessionResponse;
import com.hrms.entity.UserAccount;
import com.hrms.entity.UserActivityLog;
import com.hrms.entity.UserSession;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct Mapper for UserAccount entity and DTOs
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserAccountMapper {

    // =====================================================
    // REQUEST TO ENTITY MAPPING
    // =====================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true) // Set in service with hashing
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "failedLoginAttempts", constant = "0")
    @Mapping(target = "isActive", defaultValue = "true")
    @Mapping(target = "isLocked", constant = "false")
    @Mapping(target = "isEmailVerified", constant = "false")
    @Mapping(target = "mustChangePassword", defaultValue = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "lastLoginIp", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "passwordExpiresAt", ignore = true) // Set in service
    UserAccount toEntity(UserAccountRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "lastLoginIp", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "passwordExpiresAt", ignore = true)
    @Mapping(target = "isEmailVerified", ignore = true)
    void updateEntityFromRequest(UserAccountUpdateRequest request, @MappingTarget UserAccount userAccount);

    // =====================================================
    // ENTITY TO RESPONSE MAPPING
    // =====================================================

    @Mapping(target = "employee", source = "employee")
    UserAccountResponse toResponse(UserAccount userAccount);

    List<UserAccountResponse> toResponseList(List<UserAccount> userAccounts);

    // =====================================================
    // SESSION MAPPING
    // =====================================================

    UserSessionResponse toSessionResponse(UserSession userSession);

    List<UserSessionResponse> toSessionResponseList(List<UserSession> userSessions);

    // =====================================================
    // ACTIVITY LOG MAPPING
    // =====================================================

    @Mapping(target = "user", ignore = true)
    UserActivityLogResponse toActivityLogResponse(UserActivityLog activityLog);

    List<UserActivityLogResponse> toActivityLogResponseList(List<UserActivityLog> activityLogs);
}
