package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Response DTO for user session information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSessionResponse {

    private Long id;
    private Long userId;
    private Map<String, Object> deviceInfo;
    private String ipAddress;
    private String userAgent;
    private Boolean isActive;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastActivityAt;
}
