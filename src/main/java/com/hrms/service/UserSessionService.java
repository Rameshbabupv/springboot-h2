package com.hrms.service;

import com.hrms.dto.response.UserSessionResponse;
import com.hrms.entity.UserSession;

import java.util.List;
import java.util.Map;

/**
 * Service interface for User Session operations
 */
public interface UserSessionService {

    /**
     * Create new session
     */
    UserSession createSession(Long userId, String sessionToken, String refreshToken,
                              String ipAddress, String userAgent, Map<String, Object> deviceInfo);

    /**
     * Get session by token
     */
    UserSession getSessionByToken(String sessionToken);

    /**
     * Get all sessions for user
     */
    List<UserSessionResponse> getUserSessions(Long userId);

    /**
     * Get active sessions for user
     */
    List<UserSessionResponse> getActiveSessions(Long userId);

    /**
     * Update session activity
     */
    void updateSessionActivity(String sessionToken);

    /**
     * Terminate session
     */
    boolean terminateSession(Long sessionId);

    /**
     * Terminate all sessions for user
     */
    boolean terminateAllUserSessions(Long userId);

    /**
     * Validate session
     */
    boolean isSessionValid(String sessionToken);

    /**
     * Cleanup expired sessions
     */
    int cleanupExpiredSessions();
}
