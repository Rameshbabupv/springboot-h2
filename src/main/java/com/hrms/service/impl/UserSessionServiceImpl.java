package com.hrms.service.impl;

import com.hrms.dto.response.UserSessionResponse;
import com.hrms.entity.UserSession;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.mapper.UserAccountMapper;
import com.hrms.repository.UserSessionRepository;
import com.hrms.service.UserSessionService;
import com.hrms.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for User Session operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSessionServiceImpl implements UserSessionService {

    private final UserSessionRepository sessionRepository;
    private final UserAccountMapper mapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserSession createSession(Long userId, String sessionToken, String refreshToken,
                                     String ipAddress, String userAgent, Map<String, Object> deviceInfo) {
        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setSessionToken(sessionToken);
        session.setRefreshToken(refreshToken);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setDeviceInfo(deviceInfo);
        session.setIsActive(true);
        session.setExpiresAt(OffsetDateTime.now().plusSeconds(jwtUtil.getExpirationTime()));
        session.setLastActivityAt(OffsetDateTime.now());

        UserSession savedSession = sessionRepository.save(session);
        log.info("Session created for user: {}", userId);
        return savedSession;
    }

    @Override
    public UserSession getSessionByToken(String sessionToken) {
        return sessionRepository.findBySessionToken(sessionToken)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }

    @Override
    public List<UserSessionResponse> getUserSessions(Long userId) {
        List<UserSession> sessions = sessionRepository.findByUserId(userId);
        return mapper.toSessionResponseList(sessions);
    }

    @Override
    public List<UserSessionResponse> getActiveSessions(Long userId) {
        List<UserSession> sessions = sessionRepository
            .findByUserIdAndIsActiveTrueAndExpiresAtAfter(userId, LocalDateTime.now());
        return mapper.toSessionResponseList(sessions);
    }

    @Override
    @Transactional
    public void updateSessionActivity(String sessionToken) {
        sessionRepository.findBySessionToken(sessionToken).ifPresent(session -> {
            session.updateActivity();
            sessionRepository.save(session);
        });
    }

    @Override
    @Transactional
    public boolean terminateSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
            .map(session -> {
                session.invalidate();
                sessionRepository.save(session);
                log.info("Session terminated: {}", sessionId);
                return true;
            })
            .orElse(false);
    }

    @Override
    @Transactional
    public boolean terminateAllUserSessions(Long userId) {
        int count = sessionRepository.invalidateAllUserSessions(userId);
        log.info("Terminated {} sessions for user: {}", count, userId);
        return count > 0;
    }

    @Override
    public boolean isSessionValid(String sessionToken) {
        return sessionRepository.findBySessionToken(sessionToken)
            .map(UserSession::isValid)
            .orElse(false);
    }

    @Override
    @Transactional
    public int cleanupExpiredSessions() {
        int count = sessionRepository.deleteExpiredSessions(LocalDateTime.now().minusDays(7));
        log.info("Cleaned up {} expired sessions", count);
        return count;
    }
}
