package com.hrms.repository;

import com.hrms.entity.HolidayCompanyMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for HolidayCompanyMapping entity.
 */
@Repository
public interface HolidayCompanyMappingRepository extends JpaRepository<HolidayCompanyMapping, Long> {

    /**
     * Find all mappings for a holiday.
     */
    List<HolidayCompanyMapping> findByHolidayId(Long holidayId);

    /**
     * Find all mappings for a company.
     */
    @Query("SELECT hcm FROM HolidayCompanyMapping hcm WHERE hcm.company.id = :companyId")
    List<HolidayCompanyMapping> findByCompanyId(@Param("companyId") Long companyId);

    /**
     * Delete all mappings for a holiday.
     */
    @Modifying
    @Query("DELETE FROM HolidayCompanyMapping hcm WHERE hcm.holiday.id = :holidayId")
    void deleteByHolidayId(@Param("holidayId") Long holidayId);

    /**
     * Check if mapping exists.
     */
    @Query("SELECT COUNT(hcm) > 0 FROM HolidayCompanyMapping hcm " +
           "WHERE hcm.holiday.id = :holidayId AND hcm.company.id = :companyId")
    boolean existsByHolidayAndCompany(@Param("holidayId") Long holidayId,
                                       @Param("companyId") Long companyId);
}
