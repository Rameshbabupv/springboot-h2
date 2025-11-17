package com.hrms.repository;

import com.hrms.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByTenantId(String tenantId);

    List<Employee> findByCompanyId(Long companyId);

    Optional<Employee> findByEmpId(String empId);

    List<Employee> findByDepartmentId(Long departmentId);

    List<Employee> findByIsActiveTrue();

    List<Employee> findByEmployeeStatus(String status);
}
