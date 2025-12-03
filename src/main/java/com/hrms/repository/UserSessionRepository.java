package com.hrms.repository;

import com.hrms.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserSession entity
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    // Find session by token
    Optional<UserSession> findBySessionToken(String sessionToken);

    // Find session by refresh token
    Optional<UserSession> findByRefreshToken(String refreshToken);

    // Find all sessions for a user
    List<UserSession> findByUserId(Long userId);

    // Find active sessions for a user
    List<UserSession> findByUserIdAndIsActiveTrueAndExpiresAtAfter(Long userId, LocalDateTime currentTime);

    // Find expired sessions
    @Query("SELECT s FROM UserSession s WHERE s.expiresAt < :currentTime AND s.isActive = true")
    List<UserSession> findExpiredSessions(@Param("currentTime") LocalDateTime currentTime);

    // Count active sessions for user
    long countByUserIdAndIsActiveTrueAndExpiresAtAfter(Long userId, LocalDateTime currentTime);

    // Invalidate all sessions for a user
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.userId = :userId")
    int invalidateAllUserSessions(@Param("userId") Long userId);

    // Invalidate session by token
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.sessionToken = :token")
    int invalidateSession(@Param("token") String token);

    // Delete expired sessions (cleanup)
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :expiryDate")
    int deleteExpiredSessions(@Param("expiryDate") LocalDateTime expiryDate);
}
