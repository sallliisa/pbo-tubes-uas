package repository;

import java.util.Optional;

import domain.project.Project;

public interface ProjectRepository extends Repository<Integer, Project> {
    Optional<Project> findByIdWithClient(int projectId);
}
