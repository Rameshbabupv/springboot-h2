package com.hrms.repository;

import com.hrms.entity.ShiftBreak;
import com.hrms.enums.BreakType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ShiftBreak entity.
 */
@Repository
public interface ShiftBreakRepository extends JpaRepository<ShiftBreak, Long> {

    /**
     * Find all breaks for a shift.
     */
    List<ShiftBreak> findByShiftIdOrderByStartTime(Long shiftId);

    /**
     * Find breaks by type for a shift.
     */
    @Query("SELECT sb FROM ShiftBreak sb WHERE sb.shift.id = :shiftId AND sb.breakType = :breakType")
    List<ShiftBreak> findByShiftIdAndType(@Param("shiftId") Long shiftId,
                                           @Param("breakType") BreakType breakType);

    /**
     * Delete all breaks for a shift.
     */
    void deleteByShiftId(Long shiftId);

    /**
     * Find paid breaks for a shift.
     */
    @Query("SELECT sb FROM ShiftBreak sb WHERE sb.shift.id = :shiftId AND sb.isPaid = true")
    List<ShiftBreak> findPaidBreaksByShiftId(@Param("shiftId") Long shiftId);

    /**
     * Calculate total break duration for a shift.
     */
    @Query("SELECT COALESCE(SUM(sb.durationMinutes), 0) FROM ShiftBreak sb WHERE sb.shift.id = :shiftId")
    Integer getTotalBreakDuration(@Param("shiftId") Long shiftId);
}
