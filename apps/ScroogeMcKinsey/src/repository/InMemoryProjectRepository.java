package repository;

import java.util.Optional;

import domain.project.Project;

public class InMemoryProjectRepository extends InMemoryRepository<Integer, Project> implements ProjectRepository {
    @Override
    public Optional<Project> findByIdWithClient(int projectId) {
        return findById(projectId);
    }
}
