package com.hrms.repository;

import com.hrms.entity.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for UserActivityLog entity
 */
@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    // Find by user ID
    List<UserActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find by user ID with pagination
    Page<UserActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Find by action type
    List<UserActivityLog> findByActionTypeOrderByCreatedAtDesc(String actionType);

    // Find by user and action type
    List<UserActivityLog> findByUserIdAndActionTypeOrderByCreatedAtDesc(Long userId, String actionType);

    // Find by date range
    @Query("SELECT l FROM UserActivityLog l WHERE l.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY l.createdAt DESC")
    List<UserActivityLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Find by user and date range
    @Query("SELECT l FROM UserActivityLog l WHERE l.userId = :userId " +
           "AND l.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY l.createdAt DESC")
    List<UserActivityLog> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Find by resource
    List<UserActivityLog> findByResourceTypeAndResourceIdOrderByCreatedAtDesc(
        String resourceType,
        String resourceId
    );

    // Complex search query
    @Query("SELECT l FROM UserActivityLog l WHERE " +
           "(:userId IS NULL OR l.userId = :userId) " +
           "AND (:actionType IS NULL OR l.actionType = :actionType) " +
           "AND (:startDate IS NULL OR l.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR l.createdAt <= :endDate) " +
           "ORDER BY l.createdAt DESC")
    Page<UserActivityLog> searchActivityLogs(
        @Param("userId") Long userId,
        @Param("actionType") String actionType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    // Count activities by user
    long countByUserId(Long userId);

    // Count activities by action type
    long countByActionType(String actionType);
}
