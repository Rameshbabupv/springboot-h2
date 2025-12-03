package com.hrms.service.impl;

import com.hrms.dto.response.UserActivityLogResponse;
import com.hrms.entity.UserActivityLog;
import com.hrms.mapper.UserAccountMapper;
import com.hrms.repository.UserActivityLogRepository;
import com.hrms.service.UserActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for User Activity Log operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityLogServiceImpl implements UserActivityLogService {

    private final UserActivityLogRepository activityLogRepository;
    private final UserAccountMapper mapper;

    @Override
    @Transactional
    public void logActivity(Long userId, String actionType, String resourceType, String resourceId,
                           String description, Map<String, Object> metadata, String ipAddress, String userAgent) {
        UserActivityLog activityLog = new UserActivityLog();
        activityLog.setUserId(userId);
        activityLog.setActionType(actionType);
        activityLog.setResourceType(resourceType);
        activityLog.setResourceId(resourceId);
        activityLog.setDescription(description);
        activityLog.setMetadata(metadata);
        activityLog.setIpAddress(ipAddress);
        activityLog.setUserAgent(userAgent);

        activityLogRepository.save(activityLog);
        log.info("Activity logged: userId={}, action={}, resource={}/{}",
                 userId, actionType, resourceType, resourceId);
    }

    @Override
    public List<UserActivityLogResponse> getUserActivityLogs(Long userId, Integer limit, Integer offset) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        List<UserActivityLog> logs = activityLogRepository
            .findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .getContent();
        return mapper.toActivityLogResponseList(logs);
    }

    @Override
    public List<UserActivityLogResponse> getActivityLogsByAction(String actionType, Integer limit, Integer offset) {
        List<UserActivityLog> logs = activityLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType);
        return mapper.toActivityLogResponseList(logs.stream().skip(offset).limit(limit).toList());
    }

    @Override
    public List<UserActivityLogResponse> searchActivityLogs(Long userId, String actionType,
                                                            LocalDateTime startDate, LocalDateTime endDate,
                                                            Integer limit, Integer offset) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        List<UserActivityLog> logs = activityLogRepository
            .searchActivityLogs(userId, actionType, startDate, endDate, pageable)
            .getContent();
        return mapper.toActivityLogResponseList(logs);
    }

    @Override
    public long getActivityCount(Long userId) {
        return activityLogRepository.countByUserId(userId);
    }

    @Override
    public void logLogin(Long userId, String ipAddress, String userAgent) {
        logActivity(userId, UserActivityLog.ACTION_LOGIN, null, null,
                   "User logged in successfully", null, ipAddress, userAgent);
    }

    @Override
    public void logLogout(Long userId, String ipAddress, String userAgent) {
        logActivity(userId, UserActivityLog.ACTION_LOGOUT, null, null,
                   "User logged out", null, ipAddress, userAgent);
    }

    @Override
    public void logFailedLogin(Long userId, String username, String ipAddress, String userAgent) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("username", username);
        logActivity(userId, UserActivityLog.ACTION_LOGIN_FAILED, null, null,
                   "Failed login attempt for username: " + username, metadata, ipAddress, userAgent);
    }

    @Override
    public void logPasswordChanged(Long userId, String ipAddress, String userAgent) {
        logActivity(userId, UserActivityLog.ACTION_PASSWORD_CHANGED, null, null,
                   "Password changed successfully", null, ipAddress, userAgent);
    }

    @Override
    public void logPasswordReset(Long userId, String ipAddress, String userAgent) {
        logActivity(userId, UserActivityLog.ACTION_PASSWORD_RESET, null, null,
                   "Password reset completed", null, ipAddress, userAgent);
    }

    @Override
    public void logAccountLocked(Long userId, String reason, String ipAddress) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reason", reason);
        logActivity(userId, UserActivityLog.ACTION_ACCOUNT_LOCKED, null, null,
                   "Account locked: " + reason, metadata, ipAddress, null);
    }

    @Override
    public void logAccountUnlocked(Long userId, String ipAddress) {
        logActivity(userId, UserActivityLog.ACTION_ACCOUNT_UNLOCKED, null, null,
                   "Account unlocked", null, ipAddress, null);
    }
}
