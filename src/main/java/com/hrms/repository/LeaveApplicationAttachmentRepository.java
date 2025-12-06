package com.hrms.repository;

import com.hrms.entity.LeaveApplicationAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for LeaveApplicationAttachment entity.
 */
@Repository
public interface LeaveApplicationAttachmentRepository extends JpaRepository<LeaveApplicationAttachment, Long> {

    /**
     * Find all attachments for an application.
     */
    List<LeaveApplicationAttachment> findByLeaveApplicationId(Long applicationId);

    /**
     * Find attachments with tenant check.
     */
    @Query("SELECT a FROM LeaveApplicationAttachment a " +
           "WHERE a.tenantId = :tenantId " +
           "AND a.leaveApplication.id = :applicationId")
    List<LeaveApplicationAttachment> findByTenantAndApplicationId(@Param("tenantId") String tenantId,
                                                                   @Param("applicationId") Long applicationId);

    /**
     * Delete all attachments for an application.
     */
    void deleteByLeaveApplicationId(Long applicationId);

    /**
     * Count attachments for an application.
     */
    long countByLeaveApplicationId(Long applicationId);
}
