package com.hrms;

import com.hrms.entity.*;
import com.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final EmployeeRepository employeeRepository;
    private final DivisionRepository divisionRepository;
    private final SectionRepository sectionRepository;
    private final JobFunctionRepository jobFunctionRepository;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final GradeRepository gradeRepository;
    private final CityRepository cityRepository;
    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;

    @Override
    public void run(String... args) {
        log.info("DataInitializer starting - Loading sample data...");

        // Check if data already exists
        if (companyRepository.count() > 0) {
            log.info("Data already exists, skipping initialization");
            return;
        }

        // Sample data creation enabled
        log.info("Creating Country...");
        Country india = createCountry("India", "IN", "INR", "+91", "Republic of India");

        log.info("Creating States...");
        State tn = createState("Tamil Nadu", "TN", india.getId(), "Southernmost state");
        State ka = createState("Karnataka", "KA", india.getId(), "Silicon Valley of India");
        State kl = createState("Kerala", "KL", india.getId(), "God's Own Country");
        State ap = createState("Andhra Pradesh", "AP", india.getId(), "Rice bowl of India");
        State ts = createState("Telangana", "TS", india.getId(), "State of the Telugus");

        // Create Cities
        log.info("Creating Cities...");
        City chennai = createCity("Chennai", "CHN", india.getId(), tn.getId(), "600001");
        City coimbatore = createCity("Coimbatore", "CBE", india.getId(), tn.getId(), "641001");
        City madurai = createCity("Madurai", "MDU", india.getId(), tn.getId(), "625001");
        City bangalore = createCity("Bangalore", "BLR", india.getId(), ka.getId(), "560001");
        City mysore = createCity("Mysore", "MYS", india.getId(), ka.getId(), "570001");
        City kochi = createCity("Kochi", "COK", india.getId(), kl.getId(), "682001");
        City trivandrum = createCity("Trivandrum", "TRV", india.getId(), kl.getId(), "695001");
        City hyderabad = createCity("Hyderabad", "HYD", india.getId(), ts.getId(), "500001");
        City vijayawada = createCity("Vijayawada", "VJA", india.getId(), ap.getId(), "520001");
        City visakhapatnam = createCity("Visakhapatnam", "VTZ", india.getId(), ap.getId(), "530001");

        // Create Companies
        log.info("Creating Companies...");
        Company company1 = new Company();
        company1.setTenantId("TENANT001");
        company1.setCode("CTS001");
        company1.setIndustry("IT Services");
        company1.setName("Chennai Tech Solutions Pvt Ltd");
        company1.setShortName("CTS");
        company1.setCountry("India");
        company1.setState("Tamil Nadu");
        company1.setCity("Chennai");
        company1.setPincode("600001");
        company1.setPrimaryPhone("9876543210");
        company1.setEmail("info@chennaitech.com");
        company1.setWebsite("www.chennaitech.com");
        company1.setCompanyType("Private Limited");
        company1.setIsActive(true);
        company1 = companyRepository.save(company1);
        log.info("Created company: {}", company1.getName());

        Company company2 = new Company();
        company2.setTenantId("TENANT001");
        company2.setCode("BMC001");
        company2.setIndustry("Manufacturing");
        company2.setName("Bangalore Manufacturing Corp");
        company2.setShortName("BMC");
        company2.setCountry("India");
        company2.setState("Karnataka");
        company2.setCity("Bangalore");
        company2.setPincode("560001");
        company2.setPrimaryPhone("9876543211");
        company2.setEmail("info@bangaloremfg.com");
        company2.setCompanyType("Private Limited");
        company2.setIsActive(true);
        company2 = companyRepository.save(company2);
        log.info("Created company: {}", company2.getName());

        // Create Divisions
        log.info("Creating Divisions...");
        Division divSouth = createDivision("South Division", "SOUTH", "Southern region operations");
        Division divNorth = createDivision("North Division", "NORTH", "Northern region operations");
        Division divTech = createDivision("Technology Division", "TECH", "Technology and Innovation");
        Division divOps = createDivision("Operations Division", "OPS", "Business Operations");

        // Create Departments
        log.info("Creating Departments...");
        Department deptEng = createDepartment("Engineering", "ENG", "Software Engineering and Development");
        Department deptHR = createDepartment("Human Resources", "HR", "Human Resources Management");
        Department deptFin = createDepartment("Finance", "FIN", "Finance and Accounts");
        Department deptSales = createDepartment("Sales", "SALES", "Sales and Business Development");
        Department deptMarketing = createDepartment("Marketing", "MKT", "Marketing and Branding");

        // Create Sections
        log.info("Creating Sections...");
        Section secFrontend = createSection(deptEng, "Frontend Development", "FE", "UI/UX Development");
        Section secBackend = createSection(deptEng, "Backend Development", "BE", "Server-side Development");
        Section secDevOps = createSection(deptEng, "DevOps", "DEVOPS", "Infrastructure and Deployment");
        Section secRecruitment = createSection(deptHR, "Recruitment", "REC", "Talent Acquisition");
        Section secPayroll = createSection(deptHR, "Payroll", "PAY", "Payroll Processing");

        // Create Job Functions
        log.info("Creating Job Functions...");
        JobFunction jfDev = createJobFunction("Software Development", "DEV", "Software Development");
        JobFunction jfManagement = createJobFunction("Management", "MGMT", "People and Project Management");
        JobFunction jfSupport = createJobFunction("Support", "SUP", "Technical and Customer Support");
        JobFunction jfAdmin = createJobFunction("Administration", "ADMIN", "Administrative Functions");

        // Create Employment Types
        log.info("Creating Employment Types...");
        EmploymentType etPermanent = createEmploymentType("Permanent", "PERM", "Full-time Permanent");
        EmploymentType etContract = createEmploymentType("Contract", "CONT", "Fixed-term Contract");
        EmploymentType etIntern = createEmploymentType("Intern", "INTERN", "Internship");
        EmploymentType etConsultant = createEmploymentType("Consultant", "CONS", "Consultant");

        // Create Grades
        log.info("Creating Grades...");
        Grade gradeG1 = createGrade("Grade G1", "G1", "Entry Level");
        Grade gradeG2 = createGrade("Grade G2", "G2", "Junior Level");
        Grade gradeG3 = createGrade("Grade G3", "G3", "Mid Level");
        Grade gradeG4 = createGrade("Grade G4", "G4", "Senior Level");
        Grade gradeG5 = createGrade("Grade G5", "G5", "Lead Level");

        // Create Designations
        log.info("Creating Designations...");
        Designation desigSE = createDesignation("Software Engineer", "SE", "Software Development Engineer");
        Designation desigSSE = createDesignation("Senior Software Engineer", "SSE", "Senior Software Development Engineer");
        Designation desigTL = createDesignation("Tech Lead", "TL", "Technical Lead");
        Designation desigPM = createDesignation("Project Manager", "PM", "Project Manager");
        Designation desigHRM = createDesignation("HR Manager", "HRM", "Human Resources Manager");
        Designation desigFM = createDesignation("Finance Manager", "FM", "Finance and Accounts Manager");
        Designation desigSM = createDesignation("Sales Manager", "SM", "Sales Manager");

        // Create Employees with South Indian Names
        log.info("Creating Employees with South Indian names...");

        // Employee 1: Rajesh Kumar
        Employee emp1 = new Employee();
        emp1.setTenantId("TENANT001");
        emp1.setCompany(company1);
        emp1.setEmpId("EMP001");
        emp1.setEmployeeName("Rajesh Kumar");
        emp1.setGender("Male");
        emp1.setDateOfBirth(LocalDate.of(1990, 5, 15));
        emp1.setDateOfJoin(LocalDate.of(2020, 1, 1));
        emp1.setMobileNo("9876543210");
        emp1.setEmailId("rajesh.kumar@chennaitech.com");
        emp1.setBloodGroup("O+");
        emp1.setMaritalStatus("Married");
        emp1.setDepartment(deptEng);
        emp1.setDesignation(desigSSE);
        emp1.setBasicSalary(new BigDecimal("50000.00"));
        emp1.setGrossSalary(new BigDecimal("70000.00"));
        emp1.setCtc(new BigDecimal("840000.00"));
        emp1.setAadharNo("123456789012");
        emp1.setPanNo("ABCDE1234F");
        emp1.setEmployeeStatus("Active");
        emp1.setIsActive(true);
        emp1 = employeeRepository.save(emp1);
        log.info("Created employee: {}", emp1.getEmployeeName());

        // Employee 2: Lakshmi Priya
        Employee emp2 = new Employee();
        emp2.setTenantId("TENANT001");
        emp2.setCompany(company1);
        emp2.setEmpId("EMP002");
        emp2.setEmployeeName("Lakshmi Priya");
        emp2.setGender("Female");
        emp2.setDateOfBirth(LocalDate.of(1992, 8, 20));
        emp2.setDateOfJoin(LocalDate.of(2021, 3, 15));
        emp2.setMobileNo("9876543211");
        emp2.setEmailId("lakshmi.priya@chennaitech.com");
        emp2.setBloodGroup("A+");
        emp2.setMaritalStatus("Single");
        emp2.setDepartment(deptEng);
        emp2.setDesignation(desigSE);
        emp2.setReportingManager(emp1);
        emp2.setBasicSalary(new BigDecimal("40000.00"));
        emp2.setGrossSalary(new BigDecimal("55000.00"));
        emp2.setCtc(new BigDecimal("660000.00"));
        emp2.setAadharNo("123456789013");
        emp2.setPanNo("ABCDE1235F");
        emp2.setEmployeeStatus("Active");
        emp2.setIsActive(true);
        emp2 = employeeRepository.save(emp2);
        log.info("Created employee: {}", emp2.getEmployeeName());

        // Employee 3: Venkatesh Iyer
        Employee emp3 = new Employee();
        emp3.setTenantId("TENANT001");
        emp3.setCompany(company1);
        emp3.setEmpId("EMP003");
        emp3.setEmployeeName("Venkatesh Iyer");
        emp3.setGender("Male");
        emp3.setDateOfBirth(LocalDate.of(1988, 3, 10));
        emp3.setDateOfJoin(LocalDate.of(2019, 6, 1));
        emp3.setMobileNo("9876543212");
        emp3.setEmailId("venkatesh.iyer@chennaitech.com");
        emp3.setBloodGroup("B+");
        emp3.setMaritalStatus("Married");
        emp3.setDepartment(deptHR);
        emp3.setDesignation(desigHRM);
        emp3.setBasicSalary(new BigDecimal("60000.00"));
        emp3.setGrossSalary(new BigDecimal("85000.00"));
        emp3.setCtc(new BigDecimal("1020000.00"));
        emp3.setAadharNo("123456789014");
        emp3.setPanNo("ABCDE1236F");
        emp3.setEmployeeStatus("Active");
        emp3.setIsActive(true);
        emp3 = employeeRepository.save(emp3);
        log.info("Created employee: {}", emp3.getEmployeeName());

        // Employee 4: Sowmya Reddy
        Employee emp4 = new Employee();
        emp4.setTenantId("TENANT001");
        emp4.setCompany(company1);
        emp4.setEmpId("EMP004");
        emp4.setEmployeeName("Sowmya Reddy");
        emp4.setGender("Female");
        emp4.setDateOfBirth(LocalDate.of(1991, 11, 25));
        emp4.setDateOfJoin(LocalDate.of(2020, 9, 1));
        emp4.setMobileNo("9876543213");
        emp4.setEmailId("sowmya.reddy@chennaitech.com");
        emp4.setBloodGroup("AB+");
        emp4.setMaritalStatus("Single");
        emp4.setDepartment(deptFin);
        emp4.setDesignation(desigFM);
        emp4.setBasicSalary(new BigDecimal("55000.00"));
        emp4.setGrossSalary(new BigDecimal("75000.00"));
        emp4.setCtc(new BigDecimal("900000.00"));
        emp4.setAadharNo("123456789015");
        emp4.setPanNo("ABCDE1237F");
        emp4.setEmployeeStatus("Active");
        emp4.setIsActive(true);
        emp4 = employeeRepository.save(emp4);
        log.info("Created employee: {}", emp4.getEmployeeName());

        // Employee 5: Krishnan Nair
        Employee emp5 = new Employee();
        emp5.setTenantId("TENANT001");
        emp5.setCompany(company2);
        emp5.setEmpId("EMP005");
        emp5.setEmployeeName("Krishnan Nair");
        emp5.setGender("Male");
        emp5.setDateOfBirth(LocalDate.of(1989, 7, 18));
        emp5.setDateOfJoin(LocalDate.of(2018, 4, 1));
        emp5.setMobileNo("9876543214");
        emp5.setEmailId("krishnan.nair@bangaloremfg.com");
        emp5.setBloodGroup("O-");
        emp5.setMaritalStatus("Married");
        emp5.setDepartment(deptEng);
        emp5.setDesignation(desigSSE);
        emp5.setBasicSalary(new BigDecimal("52000.00"));
        emp5.setGrossSalary(new BigDecimal("72000.00"));
        emp5.setCtc(new BigDecimal("864000.00"));
        emp5.setAadharNo("123456789016");
        emp5.setPanNo("ABCDE1238F");
        emp5.setEmployeeStatus("Active");
        emp5.setIsActive(true);
        emp5 = employeeRepository.save(emp5);
        log.info("Created employee: {}", emp5.getEmployeeName());

        // Employee 6: Meenakshi Sundaram
        Employee emp6 = new Employee();
        emp6.setTenantId("TENANT001");
        emp6.setCompany(company1);
        emp6.setEmpId("EMP006");
        emp6.setEmployeeName("Meenakshi Sundaram");
        emp6.setGender("Female");
        emp6.setDateOfBirth(LocalDate.of(1993, 2, 14));
        emp6.setDateOfJoin(LocalDate.of(2022, 1, 10));
        emp6.setMobileNo("9876543215");
        emp6.setEmailId("meenakshi.sundaram@chennaitech.com");
        emp6.setBloodGroup("A-");
        emp6.setMaritalStatus("Married");
        emp6.setDepartment(deptSales);
        emp6.setDesignation(desigSM);
        emp6.setBasicSalary(new BigDecimal("48000.00"));
        emp6.setGrossSalary(new BigDecimal("65000.00"));
        emp6.setCtc(new BigDecimal("780000.00"));
        emp6.setAadharNo("123456789017");
        emp6.setPanNo("ABCDE1239F");
        emp6.setEmployeeStatus("Active");
        emp6.setIsActive(true);
        emp6 = employeeRepository.save(emp6);
        log.info("Created employee: {}", emp6.getEmployeeName());

        // Employee 7: Arjun Ramakrishnan
        Employee emp7 = new Employee();
        emp7.setTenantId("TENANT001");
        emp7.setCompany(company1);
        emp7.setEmpId("EMP007");
        emp7.setEmployeeName("Arjun Ramakrishnan");
        emp7.setGender("Male");
        emp7.setDateOfBirth(LocalDate.of(1994, 6, 8));
        emp7.setDateOfJoin(LocalDate.of(2022, 7, 1));
        emp7.setMobileNo("9876543216");
        emp7.setEmailId("arjun.ramakrishnan@chennaitech.com");
        emp7.setBloodGroup("B-");
        emp7.setMaritalStatus("Single");
        emp7.setDepartment(deptEng);
        emp7.setDesignation(desigSE);
        emp7.setReportingManager(emp1);
        emp7.setBasicSalary(new BigDecimal("38000.00"));
        emp7.setGrossSalary(new BigDecimal("52000.00"));
        emp7.setCtc(new BigDecimal("624000.00"));
        emp7.setAadharNo("123456789018");
        emp7.setPanNo("ABCDE1240F");
        emp7.setEmployeeStatus("Active");
        emp7.setIsActive(true);
        emp7 = employeeRepository.save(emp7);
        log.info("Created employee: {}", emp7.getEmployeeName());

        // Employee 8: Divya Bharathi
        Employee emp8 = new Employee();
        emp8.setTenantId("TENANT001");
        emp8.setCompany(company1);
        emp8.setEmpId("EMP008");
        emp8.setEmployeeName("Divya Bharathi");
        emp8.setGender("Female");
        emp8.setDateOfBirth(LocalDate.of(1995, 9, 12));
        emp8.setDateOfJoin(LocalDate.of(2023, 2, 15));
        emp8.setMobileNo("9876543217");
        emp8.setEmailId("divya.bharathi@chennaitech.com");
        emp8.setBloodGroup("O+");
        emp8.setMaritalStatus("Single");
        emp8.setDepartment(deptMarketing);
        emp8.setDesignation(desigSE);
        emp8.setBasicSalary(new BigDecimal("35000.00"));
        emp8.setGrossSalary(new BigDecimal("48000.00"));
        emp8.setCtc(new BigDecimal("576000.00"));
        emp8.setAadharNo("123456789019");
        emp8.setPanNo("ABCDE1241F");
        emp8.setEmployeeStatus("Active");
        emp8.setIsActive(true);
        emp8 = employeeRepository.save(emp8);
        log.info("Created employee: {}", emp8.getEmployeeName());

        log.info("Sample data initialization completed!");
        log.info("Total States: {}", stateRepository.count());
        log.info("Total Cities: {}", cityRepository.count());
        log.info("Total Companies: {}", companyRepository.count());
        log.info("Total Divisions: {}", divisionRepository.count());
        log.info("Total Departments: {}", departmentRepository.count());
        log.info("Total Sections: {}", sectionRepository.count());
        log.info("Total Job Functions: {}", jobFunctionRepository.count());
        log.info("Total Employment Types: {}", employmentTypeRepository.count());
        log.info("Total Grades: {}", gradeRepository.count());
        log.info("Total Designations: {}", designationRepository.count());
        log.info("Total Employees: {}", employeeRepository.count());
    }

    private Country createCountry(String name, String code, String currencyCode, String phoneCode, String description) {
        Country country = new Country();
        country.setTenantId("TENANT001");
        country.setName(name);
        country.setCode(code);
        country.setCurrencyCode(currencyCode);
        country.setPhoneCode(phoneCode);
        country.setDescription(description);
        country.setIsActive(true);
        country = countryRepository.save(country);
        log.info("Created country: {}", country.getName());
        return country;
    }

    private State createState(String name, String code, Long countryId, String description) {
        State state = new State();
        state.setTenantId("TENANT001");
        state.setCountryId(countryId);
        state.setName(name);
        state.setCode(code);
        state.setDescription(description);
        state.setIsActive(true);
        state = stateRepository.save(state);
        log.info("Created state: {}", state.getName());
        return state;
    }

    private City createCity(String name, String code, Long countryId, Long stateId, String pincode) {
        City city = new City();
        city.setTenantId("TENANT001");
        city.setName(name);
        city.setCode(code);
        city.setCountryId(countryId);
        city.setStateId(stateId);
        city.setPincode(pincode);
        city.setIsActive(true);
        city = cityRepository.save(city);
        log.info("Created city: {}", city.getName());
        return city;
    }

    private Division createDivision(String name, String code, String description) {
        Division division = new Division();
        division.setTenantId("TENANT001");
        division.setName(name);
        division.setCode(code);
        division.setDescription(description);
        division.setIsActive(true);
        division = divisionRepository.save(division);
        log.info("Created division: {}", division.getName());
        return division;
    }

    private Department createDepartment(String name, String code, String description) {
        Department department = new Department();
        department.setTenantId("TENANT001");
        department.setName(name);
        department.setCode(code);
        department.setDescription(description);
        department.setIsActive(true);
        department = departmentRepository.save(department);
        log.info("Created department: {}", department.getName());
        return department;
    }

    private Section createSection(Department department, String name, String code, String description) {
        Section section = new Section();
        section.setTenantId("TENANT001");
        section.setDepartment(department);
        section.setName(name);
        section.setCode(code);
        section.setDescription(description);
        section.setIsActive(true);
        section = sectionRepository.save(section);
        log.info("Created section: {}", section.getName());
        return section;
    }

    private JobFunction createJobFunction(String name, String code, String description) {
        JobFunction jobFunction = new JobFunction();
        jobFunction.setTenantId("TENANT001");
        jobFunction.setName(name);
        jobFunction.setCode(code);
        jobFunction.setDescription(description);
        jobFunction.setIsActive(true);
        jobFunction = jobFunctionRepository.save(jobFunction);
        log.info("Created job function: {}", jobFunction.getName());
        return jobFunction;
    }

    private EmploymentType createEmploymentType(String name, String code, String description) {
        EmploymentType employmentType = new EmploymentType();
        employmentType.setTenantId("TENANT001");
        employmentType.setName(name);
        employmentType.setCode(code);
        employmentType.setDescription(description);
        employmentType.setIsActive(true);
        employmentType = employmentTypeRepository.save(employmentType);
        log.info("Created employment type: {}", employmentType.getName());
        return employmentType;
    }

    private Grade createGrade(String name, String code, String description) {
        Grade grade = new Grade();
        grade.setTenantId("TENANT001");
        grade.setName(name);
        grade.setCode(code);
        grade.setDescription(description);
        grade.setIsActive(true);
        grade = gradeRepository.save(grade);
        log.info("Created grade: {}", grade.getName());
        return grade;
    }

    private Designation createDesignation(String name, String code, String description) {
        Designation designation = new Designation();
        designation.setTenantId("TENANT001");
        designation.setName(name);
        designation.setCode(code);
        designation.setDescription(description);
        designation.setIsActive(true);
        designation = designationRepository.save(designation);
        log.info("Created designation: {}", designation.getName());
        return designation;
    }
}
