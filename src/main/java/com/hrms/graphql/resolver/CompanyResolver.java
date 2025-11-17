package com.hrms.graphql.resolver;

import com.hrms.entity.Company;
import com.hrms.graphql.input.CompanyInput;
import com.hrms.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CompanyResolver {

    private final CompanyRepository companyRepository;

    @QueryMapping
    public List<Company> companies() {
        return companyRepository.findAll();
    }

    @QueryMapping
    public Company company(@Argument Long id) {
        return companyRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Company> companiesByTenant(@Argument String tenantId) {
        return companyRepository.findByTenantId(tenantId);
    }

    @QueryMapping
    public List<Company> activeCompanies() {
        return companyRepository.findByIsActiveTrue();
    }

    @MutationMapping
    public Company createCompany(@Argument CompanyInput input) {
        Company company = mapToEntity(input);
        return companyRepository.save(company);
    }

    @MutationMapping
    public Company updateCompany(@Argument Long id, @Argument CompanyInput input) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        updateEntityFromInput(company, input);
        return companyRepository.save(company);
    }

    @MutationMapping
    public Boolean deleteCompany(@Argument Long id) {
        if (companyRepository.existsById(id)) {
            companyRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Company mapToEntity(CompanyInput input) {
        Company company = new Company();
        updateEntityFromInput(company, input);
        return company;
    }

    private void updateEntityFromInput(Company company, CompanyInput input) {
        company.setTenantId(input.getTenantId());
        company.setIndustry(input.getIndustry());
        company.setCompanyName(input.getCompanyName());
        company.setShortName(input.getShortName());
        company.setLogoUrl(input.getLogoUrl());
        company.setAddressLine1(input.getAddressLine1());
        company.setAddressLine2(input.getAddressLine2());
        company.setCountry(input.getCountry());
        company.setState(input.getState());
        company.setCity(input.getCity());
        company.setPincode(input.getPincode());
        company.setPrimaryPhone(input.getPrimaryPhone());
        company.setEmail(input.getEmail());
        company.setWebsite(input.getWebsite());
        company.setGstNumber(input.getGstNumber());
        company.setPan(input.getPan());
        company.setTan(input.getTan());
        company.setCin(input.getCin());
        if (input.getIncorporationDate() != null && !input.getIncorporationDate().isEmpty()) {
            company.setIncorporationDate(LocalDate.parse(input.getIncorporationDate()));
        }
        company.setCompanyType(input.getCompanyType());
        company.setIsActive(input.getIsActive());
    }
}
