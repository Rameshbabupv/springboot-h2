package com.hrms.repository;

import com.hrms.entity.Holiday;
import com.hrms.enums.HolidayCategory;
import com.hrms.enums.HolidayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Holiday entity.
 */
@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    /**
     * Find all holidays for a tenant in a year.
     */
    @Query("SELECT h FROM Holiday h WHERE h.tenantId = :tenantId " +
           "AND YEAR(h.date) = :year ORDER BY h.date")
    List<Holiday> findByTenantAndYear(@Param("tenantId") String tenantId,
                                       @Param("year") int year);

    /**
     * Find holidays for a company within a date range.
     */
    @Query("SELECT DISTINCT h FROM Holiday h " +
           "LEFT JOIN h.companyMappings cm " +
           "WHERE h.tenantId = :tenantId " +
           "AND h.date BETWEEN :startDate AND :endDate " +
           "AND (cm IS NULL OR cm.company.id = :companyId) " +
           "ORDER BY h.date")
    List<Holiday> findByTenantAndCompanyAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find holidays for a location within a date range.
     */
    @Query("SELECT DISTINCT h FROM Holiday h " +
           "LEFT JOIN h.locationMappings lm " +
           "WHERE h.tenantId = :tenantId " +
           "AND h.date BETWEEN :startDate AND :endDate " +
           "AND (lm IS NULL OR lm.location.id = :locationId) " +
           "ORDER BY h.date")
    List<Holiday> findByTenantAndLocationAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("locationId") Long locationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find holiday by date for a tenant.
     */
    @Query("SELECT h FROM Holiday h WHERE h.tenantId = :tenantId AND h.date = :date")
    List<Holiday> findByTenantAndDate(@Param("tenantId") String tenantId,
                                       @Param("date") LocalDate date);

    /**
     * Find mandatory holidays for a tenant.
     */
    @Query("SELECT h FROM Holiday h WHERE h.tenantId = :tenantId " +
           "AND h.isMandatory = true AND YEAR(h.date) = :year ORDER BY h.date")
    List<Holiday> findMandatoryByTenantAndYear(@Param("tenantId") String tenantId,
                                                @Param("year") int year);

    /**
     * Find floating holidays for a tenant.
     */
    @Query("SELECT h FROM Holiday h WHERE h.tenantId = :tenantId " +
           "AND h.isFloating = true AND YEAR(h.date) = :year ORDER BY h.date")
    List<Holiday> findFloatingByTenantAndYear(@Param("tenantId") String tenantId,
                                               @Param("year") int year);

    /**
     * Find NI Act compliant holidays.
     */
    @Query("SELECT h FROM Holiday h WHERE h.tenantId = :tenantId " +
           "AND h.isNiActCompliant = true AND YEAR(h.date) = :year ORDER BY h.date")
    List<Holiday> findNiActCompliantByTenantAndYear(@Param("tenantId") String tenantId,
                                                     @Param("year") int year);

    /**
     * Find holidays by type.
     */
    @Query("SELECT h FROM Holiday h WHERE h.tenantId = :tenantId " +
           "AND h.holidayType = :holidayType AND YEAR(h.date) = :year ORDER BY h.date")
    List<Holiday> findByTenantAndTypeAndYear(@Param("tenantId") String tenantId,
                                              @Param("holidayType") HolidayType holidayType,
                                              @Param("year") int year);

    /**
     * Find holidays by category.
     */
    @Query("SELECT h FROM Holiday h WHERE h.tenantId = :tenantId " +
           "AND h.category = :category AND YEAR(h.date) = :year ORDER BY h.date")
    List<Holiday> findByTenantAndCategoryAndYear(@Param("tenantId") String tenantId,
                                                  @Param("category") HolidayCategory category,
                                                  @Param("year") int year);

    /**
     * Check if a date is a holiday for a company.
     */
    @Query("SELECT COUNT(h) > 0 FROM Holiday h " +
           "LEFT JOIN h.companyMappings cm " +
           "WHERE h.tenantId = :tenantId AND h.date = :date " +
           "AND (cm IS NULL OR cm.company.id = :companyId)")
    boolean isHolidayForCompany(@Param("tenantId") String tenantId,
                                 @Param("companyId") Long companyId,
                                 @Param("date") LocalDate date);

    /**
     * Check if a date is a holiday for a location.
     */
    @Query("SELECT COUNT(h) > 0 FROM Holiday h " +
           "LEFT JOIN h.locationMappings lm " +
           "WHERE h.tenantId = :tenantId AND h.date = :date " +
           "AND (lm IS NULL OR lm.location.id = :locationId)")
    boolean isHolidayForLocation(@Param("tenantId") String tenantId,
                                  @Param("locationId") Long locationId,
                                  @Param("date") LocalDate date);

    /**
     * Find holiday with mappings eagerly loaded.
     */
    @Query("SELECT h FROM Holiday h " +
           "LEFT JOIN FETCH h.companyMappings " +
           "LEFT JOIN FETCH h.locationMappings " +
           "WHERE h.id = :id")
    Holiday findByIdWithMappings(@Param("id") Long id);

    /**
     * Count holidays in a year for a tenant.
     */
    @Query("SELECT COUNT(h) FROM Holiday h WHERE h.tenantId = :tenantId AND YEAR(h.date) = :year")
    long countByTenantAndYear(@Param("tenantId") String tenantId,
                               @Param("year") int year);

    /**
     * Find holidays with flexible filtering for frontend.
     * Supports optional filters: companyId, year, locationId, searchQuery
     */
    @Query("SELECT DISTINCT h FROM Holiday h " +
           "LEFT JOIN h.companyMappings cm " +
           "LEFT JOIN h.locationMappings lm " +
           "WHERE h.tenantId = :tenantId " +
           "AND (:year IS NULL OR YEAR(h.date) = :year) " +
           "AND (:companyId IS NULL OR cm IS NULL OR cm.company.id = :companyId) " +
           "AND (:locationId IS NULL OR lm IS NULL OR lm.location.id = :locationId) " +
           "AND (:searchQuery IS NULL OR :searchQuery = '' OR " +
           "     LOWER(h.name) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) " +
           "ORDER BY h.date")
    List<Holiday> findHolidaysWithFilters(@Param("tenantId") String tenantId,
                                           @Param("companyId") Long companyId,
                                           @Param("year") Integer year,
                                           @Param("locationId") Long locationId,
                                           @Param("searchQuery") String searchQuery);
}
