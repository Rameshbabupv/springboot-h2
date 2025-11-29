package com.hrms.service;

import com.hrms.dto.response.UserActivityLogResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for User Activity Log operations
 */
public interface UserActivityLogService {

    /**
     * Log user activity
     */
    void logActivity(Long userId, String actionType, String resourceType, String resourceId,
                    String description, Map<String, Object> metadata, String ipAddress, String userAgent);

    /**
     * Get activity logs for user
     */
    List<UserActivityLogResponse> getUserActivityLogs(Long userId, Integer limit, Integer offset);

    /**
     * Get activity logs by action type
     */
    List<UserActivityLogResponse> getActivityLogsByAction(String actionType, Integer limit, Integer offset);

    /**
     * Search activity logs with filters
     */
    List<UserActivityLogResponse> searchActivityLogs(Long userId, String actionType,
                                                     LocalDateTime startDate, LocalDateTime endDate,
                                                     Integer limit, Integer offset);

    /**
     * Get activity count for user
     */
    long getActivityCount(Long userId);

    // Helper methods for common activities
    void logLogin(Long userId, String ipAddress, String userAgent);
    void logLogout(Long userId, String ipAddress, String userAgent);
    void logFailedLogin(Long userId, String username, String ipAddress, String userAgent);
    void logPasswordChanged(Long userId, String ipAddress, String userAgent);
    void logPasswordReset(Long userId, String ipAddress, String userAgent);
    void logAccountLocked(Long userId, String reason, String ipAddress);
    void logAccountUnlocked(Long userId, String ipAddress);
}
