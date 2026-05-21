package service;

import java.math.BigDecimal;
import java.time.LocalDate;

import domain.organization.Client;
import domain.organization.Employee;
import domain.project.Project;
import domain.project.ProjectAssignment;
import domain.project.ProjectStatus;
import repository.ProjectRepository;

public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project createProject(
        int projectId,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        ProjectStatus status,
        BigDecimal budget
    ) {
        Project project = new Project(projectId, name, description, startDate, endDate, status, budget);
        projectRepository.save(project);
        return project;
    }

    public void addProjectToClient(Client client, Project project) {
        client.addProject(project);
        projectRepository.save(project);
    }

    public ProjectAssignment assignEmployee(
        Project project,
        Employee employee,
        String roleOnProject,
        LocalDate assignmentStartDate,
        LocalDate assignmentEndDate
    ) {
        projectRepository.save(project);
        return project.assignEmployee(employee, roleOnProject, assignmentStartDate, assignmentEndDate);
    }

    public ProjectAssignment assignEmployee(Project project, Employee employee, String roleOnProject) {
        projectRepository.save(project);
        return project.assignEmployee(employee, roleOnProject);
    }

    public void updateProjectSchedule(Project project, LocalDate startDate, LocalDate endDate) {
        project.updateSchedule(startDate, endDate);
        projectRepository.save(project);
    }

    public void updateProjectBudget(Project project, BigDecimal budget) {
        project.updateBudget(budget);
        projectRepository.save(project);
    }

    public void updateProjectStatus(Project project, ProjectStatus status) {
        project.changeStatus(status);
        projectRepository.save(project);
    }

    public void save(Project project) {
        projectRepository.save(project);
    }
}
