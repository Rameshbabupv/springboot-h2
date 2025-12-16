package com.hrms.graphql.resolver;

import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.BiometricSyncService;
import com.hrms.service.BiometricSyncService.BiometricSyncResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * GraphQL resolver for biometric data sync operations.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class BiometricSyncResolver {

    private final BiometricSyncService biometricSyncService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    /**
     * Sync biometric data from Supabase to HRMS punch_logs.
     */
    @MutationMapping
    public BiometricSyncResult syncBiometricData(@Argument(name = "tenantId") String tenantIdArg,
                                                  @Argument Long companyId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        log.info("GraphQL: syncBiometricData called for tenant: {}, company: {}", tenantId, companyId);
        return biometricSyncService.syncFromSupabase(tenantId, companyId);
    }

    /**
     * Test connection to Supabase.
     */
    @MutationMapping
    public Boolean testSupabaseConnection() {
        log.info("GraphQL: testSupabaseConnection called");
        return biometricSyncService.testSupabaseConnection();
    }

    /**
     * Get count of records in Supabase (for monitoring).
     */
    @QueryMapping
    public Integer supabaseRecordCount() {
        return biometricSyncService.getSupabaseRecordCount();
    }
}
