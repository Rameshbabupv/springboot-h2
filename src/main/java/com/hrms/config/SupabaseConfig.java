package com.hrms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for Supabase connection.
 * Used for syncing biometric data from Supabase cloud.
 */
@Configuration
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.table}")
    private String supabaseTable;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getSupabaseUrl() {
        return supabaseUrl;
    }

    public String getSupabaseKey() {
        return supabaseKey;
    }

    public String getSupabaseTable() {
        return supabaseTable;
    }

    public String getBiometricDataEndpoint() {
        return supabaseUrl + "/rest/v1/" + supabaseTable;
    }
}
