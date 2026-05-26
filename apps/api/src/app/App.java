package app;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import domain.billing.Contract;
import domain.billing.Invoice;
import domain.common.Signable;
import domain.organization.Client;
import domain.organization.ContractEmployee;
import domain.organization.Employee;
import domain.organization.Department;
import domain.organization.PermanentEmployee;
import domain.organization.Position;
import domain.project.Project;
import domain.project.ProjectAssignment;
import domain.project.ProjectStatus;
import domain.timesheet.Timesheet;
import domain.timesheet.TimesheetEntry;
import domain.timesheet.TimesheetStatus;
import exceptions.DomainException;
import persistence.DatabaseConnection;
import persistence.jdbc.JdbcClientRepository;
import persistence.jdbc.JdbcContractRepository;
import persistence.jdbc.JdbcDepartmentRepository;
import persistence.jdbc.JdbcEmployeeRepository;
import persistence.jdbc.JdbcInvoiceRepository;
import persistence.jdbc.JdbcPositionRepository;
import persistence.jdbc.JdbcProjectRepository;
import persistence.jdbc.JdbcTimesheetRepository;
import repository.ContractRepository;
import repository.EmployeeRepository;
import repository.InvoiceRepository;
import repository.ProjectRepository;
import repository.Repository;
import service.BillingService;
import service.EmployeeService;
import service.PayrollService;
import service.ProjectService;
import service.ReportService;
import service.SigningService;
import service.TimesheetService;
import workflow.ApprovalQueue;

public class App {
    public static void main(String[] args) {
        try {
            EmployeeRepository employeeRepository;
            ProjectRepository projectRepository;
            InvoiceRepository invoiceRepository;
            JdbcTimesheetRepository timesheetJdbcRepository = null;
            ContractRepository contractRepository = null;

            printSection("Repository Initialization");
            Connection connection = DatabaseConnection.getConnection();
            employeeRepository = new JdbcEmployeeRepository(connection);
            projectRepository = new JdbcProjectRepository(connection);
            invoiceRepository = new JdbcInvoiceRepository(connection);
            timesheetJdbcRepository = new JdbcTimesheetRepository(connection);
            contractRepository = new JdbcContractRepository(connection);
            JdbcDepartmentRepository departmentRepository = new JdbcDepartmentRepository(connection);
            JdbcPositionRepository positionRepository = new JdbcPositionRepository(connection);
            JdbcClientRepository clientRepository = new JdbcClientRepository(connection);
            System.out.println("JDBC repositories active.");

            printSection("JDBC Seed Verification");
            System.out.println("Employees loaded: " + employeeRepository.findAll().size());
            System.out.println("Employee 1001 exists: " + employeeRepository.findById(1001).isPresent());
            System.out.println("Projects loaded: " + projectRepository.findAll().size());
            System.out.println("Project 7001 with client exists: " + projectRepository.findByIdWithClient(7001).isPresent());
            if (timesheetJdbcRepository != null) {
                List<Timesheet> seededTimesheets = timesheetJdbcRepository.findAll();
                System.out.println("Timesheets loaded: " + seededTimesheets.size());
                if (!seededTimesheets.isEmpty()) {
                    System.out.println("First timesheet total hours: " + seededTimesheets.get(0).getTotalHours());
                }
            }
            System.out.println("Invoices for project 7001: " + invoiceRepository.findByProjectId(7001).size());
            if (contractRepository != null) {
                System.out.println("Contracts for project 7001: " + contractRepository.findByProjectId(7001).size());
            }
            System.out.println("Departments loaded: " + departmentRepository.findAll().size());
            System.out.println("Positions loaded: " + positionRepository.findAll().size());
            System.out.println("Clients loaded: " + clientRepository.findAll().size());

            printSection("Service Initialization");
            EmployeeService employeeService = new EmployeeService(employeeRepository);
            ProjectService projectService = new ProjectService(projectRepository);
            TimesheetService timesheetService = new TimesheetService();
            BillingService billingService = new BillingService(invoiceRepository);
            SigningService signingService = new SigningService();
            PayrollService payrollService = new PayrollService();
            ReportService reportService = new ReportService();

            printSection("Organization Setup");
            Department consulting = new Department(1, "Business Consulting");
            Position seniorConsultant = new Position(
                1,
                "Senior Consultant",
                "Senior",
                new BigDecimal("6000.00"),
                new BigDecimal("13000.00"),
                "Leads delivery workstreams and mentors consultants"
            );
            consulting.addPosition(seniorConsultant);
            System.out.println("Department and position ready.");

            PermanentEmployee manager = new PermanentEmployee(
                1001,
                "Alicia",
                "Tan",
                "alicia.tan@example.com",
                LocalDate.of(2022, 1, 10),
                new BigDecimal("9000.00"),
                "Platinum",
                15
            );
            ContractEmployee consultant = new ContractEmployee(
                2001,
                "John",
                "Doe",
                "john.doe@contractor.example",
                LocalDate.of(2025, 1, 1),
                new BigDecimal("7000.00"),
                LocalDate.of(2026, 1, 31),
                LocalDate.of(2027, 3, 31)
            );

            employeeService.registerEmployee(manager, consulting, seniorConsultant);
            employeeService.registerEmployee(consultant, consulting, seniorConsultant);
            employeeService.assignDepartmentManager(consulting, manager);

            printSection("Generic Repository Demo");
            employeeService.save(manager);
            employeeService.save(consultant);
            Optional<Employee> foundManager = employeeService.findEmployee(1001);
            System.out.println("Employee 1001 found: " + foundManager.map(Employee::getFullName).orElse("-"));
            System.out.println("Total employees in repository: " + employeeService.findAllEmployees().size());

            printSection("Client and Project Setup");
            Client client = new Client(
                501,
                "Northwind Holdings",
                "Retail",
                "Marcus Lee",
                "marcus.lee@northwindholdings.example",
                "+62-21-555-0199"
            );
            Project transformation = projectService.createProject(
                7001,
                "ERP Transformation",
                "Replace legacy ERP and streamline finance operations",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 7, 31),
                ProjectStatus.Active,
                new BigDecimal("175000.00")
            );
            projectService.addProjectToClient(client, transformation);
            projectService.save(transformation);

            printSection("Project Assignment Demo");
            ProjectAssignment managerAssignment = projectService.assignEmployee(
                transformation,
                manager,
                "Project Manager",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 7, 31)
            );
            ProjectAssignment consultantAssignment = projectService.assignEmployee(
                transformation,
                consultant,
                "Consultant"
            );
            System.out.println("Manager assignment duration: " + managerAssignment.getAssignmentDuration());
            System.out.println("Consultant assignment active today: " + consultantAssignment.isActive(LocalDate.now()));

            printSection("Overloading Demo");
            Timesheet approvedTimesheet = timesheetService.createTimesheet(
                consultant,
                transformation,
                LocalDate.of(2026, 3, 16),
                LocalDate.of(2026, 3, 31)
            );
            TimesheetEntry discoveryEntry = new TimesheetEntry(
                3,
                LocalDate.of(2026, 3, 18),
                new BigDecimal("8.0"),
                true,
                "Discovery interviews"
            );
            timesheetService.addEntry(approvedTimesheet, discoveryEntry);
            timesheetService.addEntry(
                approvedTimesheet,
                4,
                LocalDate.of(2026, 3, 19),
                new BigDecimal("6.5"),
                true,
                "Process mapping"
            );
            timesheetService.submit(approvedTimesheet);
            timesheetService.approve(approvedTimesheet);
            System.out.println("Approved timesheet billable hours: " + approvedTimesheet.getBillableHours());

            Contract masterServicesAgreement = billingService.createContract(
                transformation,
                9001,
                "Master Services Agreement",
                LocalDate.of(2025, 12, 20),
                "Awaiting signature package",
                "MSA-2026-001",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("180000.00"),
                "Monthly billing based on approved billable work"
            );
            signingService.sign(masterServicesAgreement, "Legal Counsel");
            masterServicesAgreement.renew(LocalDate.of(2027, 6, 30), new BigDecimal("220000.00"));
            Contract extensionContract = new Contract(
                9003,
                "Extension Agreement",
                LocalDate.of(2026, 6, 1),
                "Extension option",
                "EXT-2026-010",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("60000.00"),
                "Covers post-go-live support"
            );
            extensionContract.sign("Legal Counsel");
            extensionContract.renew(LocalDate.of(2027, 3, 31));
            System.out.println("Contract renew overload demonstrated.");

            printSection("Invoice Workflow");
            Invoice invoice = billingService.createInvoice(
                transformation,
                8101,
                "March Billing",
                LocalDate.of(2026, 4, 1),
                "Prepared after approved consultant timesheet",
                BigDecimal.ZERO
            );
            billingService.generateInvoiceFromTimesheet(invoice, approvedTimesheet, new BigDecimal("150.00"));
            invoice.setNotes("Generated from approved March consultant timesheet");
            signingService.sign(invoice, "Finance Lead");
            invoice.markSent();
            invoice.markPaid();
            billingService.saveInvoice(invoice);
            System.out.println("Project total billed amount: " + transformation.getTotalBilledAmount());

            printSection("Inclusion Polymorphism Demo");
            List<Employee> mixedEmployees = List.of(manager, consultant);
            List<BigDecimal> compensations = payrollService.calculateCompensations(mixedEmployees);
            for (int i = 0; i < mixedEmployees.size(); i++) {
                Employee employee = mixedEmployees.get(i);
                System.out.println(employee.getEmployeeType() + " compensation: " + compensations.get(i));
            }
            System.out.println("Total payroll: " + payrollService.calculateTotalCompensation(mixedEmployees));

            printSection("Interface Polymorphism Demo");
            Contract signableContract = new Contract(
                9901,
                "Polymorphism Demo Contract",
                LocalDate.of(2026, 4, 5),
                "Demo signing via Signable",
                "POLY-CONTRACT-01",
                LocalDate.of(2026, 4, 5),
                LocalDate.of(2026, 9, 30),
                new BigDecimal("50000.00"),
                "Demo terms"
            );
            Invoice signableInvoice = new Invoice(
                9902,
                "Polymorphism Demo Invoice",
                LocalDate.of(2026, 4, 6),
                "Demo signing via Signable",
                BigDecimal.ZERO
            );
            signableInvoice.generateFromTimesheet(approvedTimesheet, new BigDecimal("150.00"));
            List<Signable> signables = List.of(signableContract, signableInvoice);
            signingService.signAll(signables, "Finance and Legal Lead");
            for (String signableStatus : reportService.map(
                signables,
                signable -> signable.getClass().getSimpleName() + " signed=" + signable.isSigned()
            )) {
                System.out.println(signableStatus);
            }

            printSection("Generic Methods Demo");
            List<Project> activeProjects = reportService.filter(
                projectRepository.findAll(),
                project -> project.getStatus() == ProjectStatus.Active
            );
            List<String> activeProjectSummaries = reportService.map(
                activeProjects,
                project -> project.getName() + " (" + project.getStatus() + ")"
            );
            List<Timesheet> approvedTimesheets = reportService.filter(
                List.of(approvedTimesheet),
                timesheet -> timesheet.getStatus() == TimesheetStatus.Approved
            );
            List<String> employeeNames = reportService.map(mixedEmployees, Employee::getFullName);
            List<BigDecimal> approvedHours = reportService.map(approvedTimesheets, Timesheet::getTotalHours);
            System.out.println("Active projects: " + activeProjectSummaries);
            System.out.println("Employee names: " + employeeNames);
            System.out.println("Approved timesheet hours: " + approvedHours);

            printSection("Upper-Bounded Generics Demo");
            List<PermanentEmployee> permanentEmployees = List.of(manager);
            List<ContractEmployee> contractEmployees = List.of(consultant);
            System.out.println(
                "Permanent payroll total: " + payrollService.calculateTotalCompensation(permanentEmployees)
            );
            System.out.println(
                "Contract payroll total: " + payrollService.calculateTotalCompensation(contractEmployees)
            );

            printSection("Lower-Bounded Generics Demo");
            List<Employee> companyEmployees = new ArrayList<>();
            employeeService.copyEmployees(permanentEmployees, companyEmployees);
            employeeService.copyEmployees(contractEmployees, companyEmployees);
            System.out.println("Company employees after copy: " + companyEmployees.size());

            printSection("Generic Approval Queue Demo");
            ApprovalQueue<Timesheet> timesheetQueue = new ApprovalQueue<>();
            timesheetQueue.submit(approvedTimesheet);
            System.out.println("Timesheet queue size: " + timesheetQueue.size());
            System.out.println("Timesheet dequeued: " + timesheetQueue.next().map(Timesheet::getId).orElse(-1));

            ApprovalQueue<Invoice> invoiceQueue = new ApprovalQueue<>();
            invoiceQueue.submit(invoice);
            System.out.println("Invoice queue size: " + invoiceQueue.size());
            System.out.println("Invoice dequeued: " + invoiceQueue.next().map(Invoice::getId).orElse(-1));

            ApprovalQueue<Contract> contractQueue = new ApprovalQueue<>();
            contractQueue.submit(masterServicesAgreement);
            System.out.println("Contract queue size: " + contractQueue.size());
            System.out.println("Contract dequeued: " + contractQueue.next().map(Contract::getId).orElse(-1));

            printSection("Final Consulting Company Workflow Summary");
            System.out.println("Department: " + consulting.getName());
            System.out.println("Client active projects: " + client.getActiveProjects().size());
            System.out.println("Approved timesheets: " + approvedTimesheets.size());
            System.out.println("Signed documents: " + signables.size());
            System.out.println("Repository mode: JDBC");
            System.out.println("Workflow completed with service-oriented orchestration.");
        } catch (DomainException e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println();
            printSection("End of Program");
        }
    }

    private static void printSection(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }

    private static void printPadding() {
        System.out.println();
    }
}
