package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Response DTO for user activity log
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityLogResponse {

    private Long id;
    private Long userId;
    private UserAccountResponse user;
    private String actionType;
    private String resourceType;
    private String resourceId;
    private String description;
    private Map<String, Object> metadata;
    private String ipAddress;
    private String userAgent;
    private OffsetDateTime createdAt;
}
