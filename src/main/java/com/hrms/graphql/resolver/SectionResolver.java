package com.hrms.graphql.resolver;

import com.hrms.entity.Department;
import com.hrms.entity.Section;
import com.hrms.graphql.input.SectionInput;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.SectionRepository;
import com.hrms.security.JwtClaimsExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Resolver for Section operations.
 *
 * JWT Integration:
 * - tenantId is extracted from JWT token (preferred)
 * - @Argument tenantId kept for backward compatibility during migration
 * - JWT takes precedence when available
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class SectionResolver {

    private final SectionRepository sectionRepository;
    private final DepartmentRepository departmentRepository;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    @QueryMapping
    public List<Section> sections() {
        return sectionRepository.findAll();
    }

    @QueryMapping
    public Section section(@Argument Long id) {
        return sectionRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Section> sectionsByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return sectionRepository.findByTenantId(tenantId);
    }

    @QueryMapping
    public List<Section> sectionsByDepartment(@Argument Long departmentId) {
        return sectionRepository.findByDepartmentId(departmentId);
    }

    @QueryMapping
    public List<Section> activeSections() {
        return sectionRepository.findByIsActiveTrue();
    }

    @MutationMapping
    public Section createSection(@Argument SectionInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        Section section = mapToEntity(input, tenantId);
        return sectionRepository.save(section);
    }

    @MutationMapping
    public Section updateSection(@Argument Long id, @Argument SectionInput input) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        updateEntityFromInput(section, input, tenantId);
        return sectionRepository.save(section);
    }

    @MutationMapping
    public Boolean deleteSection(@Argument Long id) {
        if (sectionRepository.existsById(id)) {
            sectionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Section mapToEntity(SectionInput input, String tenantId) {
        Section section = new Section();
        updateEntityFromInput(section, input, tenantId);
        return section;
    }

    private void updateEntityFromInput(Section section, SectionInput input, String tenantId) {
        section.setTenantId(tenantId);
        section.setName(input.getName());
        section.setCode(input.getCode());
        section.setDescription(input.getDescription());
        section.setIsActive(input.getIsActive());

        if (input.getDepartmentId() != null) {
            Department department = departmentRepository.findById(input.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            section.setDepartment(department);
        }
    }
}
