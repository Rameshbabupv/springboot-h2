package com.hrms.repository;

import com.hrms.entity.HolidayLocationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for HolidayLocationMapping entity.
 */
@Repository
public interface HolidayLocationMappingRepository extends JpaRepository<HolidayLocationMapping, Long> {

    /**
     * Find all mappings for a holiday.
     */
    List<HolidayLocationMapping> findByHolidayId(Long holidayId);

    /**
     * Find all mappings for a location.
     */
    @Query("SELECT hlm FROM HolidayLocationMapping hlm WHERE hlm.location.id = :locationId")
    List<HolidayLocationMapping> findByLocationId(@Param("locationId") Long locationId);

    /**
     * Delete all mappings for a holiday.
     */
    @Modifying
    @Query("DELETE FROM HolidayLocationMapping hlm WHERE hlm.holiday.id = :holidayId")
    void deleteByHolidayId(@Param("holidayId") Long holidayId);

    /**
     * Check if mapping exists.
     */
    @Query("SELECT COUNT(hlm) > 0 FROM HolidayLocationMapping hlm " +
           "WHERE hlm.holiday.id = :holidayId AND hlm.location.id = :locationId")
    boolean existsByHolidayAndLocation(@Param("holidayId") Long holidayId,
                                        @Param("locationId") Long locationId);
}
