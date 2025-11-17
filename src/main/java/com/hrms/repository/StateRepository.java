package com.hrms.repository;

import com.hrms.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {

    List<State> findByTenantId(String tenantId);

    Optional<State> findByTenantIdAndCode(String tenantId, String code);

    List<State> findByIsActiveTrue();
}
