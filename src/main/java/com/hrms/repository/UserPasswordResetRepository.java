package com.hrms.repository;

import com.hrms.entity.UserPasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserPasswordReset entity
 */
@Repository
public interface UserPasswordResetRepository extends JpaRepository<UserPasswordReset, Long> {

    // Find by reset token
    Optional<UserPasswordReset> findByResetToken(String resetToken);

    // Find valid token (not used and not expired)
    @Query("SELECT r FROM UserPasswordReset r WHERE r.resetToken = :token " +
           "AND r.usedAt IS NULL " +
           "AND r.expiresAt > :currentTime")
    Optional<UserPasswordReset> findValidToken(
        @Param("token") String token,
        @Param("currentTime") LocalDateTime currentTime
    );

    // Find all tokens for a user
    List<UserPasswordReset> findByUserId(Long userId);

    // Find active tokens for a user (not used and not expired)
    @Query("SELECT r FROM UserPasswordReset r WHERE r.userId = :userId " +
           "AND r.usedAt IS NULL " +
           "AND r.expiresAt > :currentTime")
    List<UserPasswordReset> findActiveTokensForUser(
        @Param("userId") Long userId,
        @Param("currentTime") LocalDateTime currentTime
    );

    // Invalidate all active tokens for a user (when password is reset)
    @Modifying
    @Query("UPDATE UserPasswordReset r SET r.usedAt = :usedAt " +
           "WHERE r.userId = :userId AND r.usedAt IS NULL")
    int invalidateAllUserTokens(
        @Param("userId") Long userId,
        @Param("usedAt") LocalDateTime usedAt
    );

    // Delete expired tokens (cleanup)
    @Modifying
    @Query("DELETE FROM UserPasswordReset r WHERE r.expiresAt < :expiryDate")
    int deleteExpiredTokens(@Param("expiryDate") LocalDateTime expiryDate);
}
