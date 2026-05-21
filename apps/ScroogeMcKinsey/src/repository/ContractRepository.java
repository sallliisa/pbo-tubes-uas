package repository;

import java.util.List;

import domain.billing.Contract;

public interface ContractRepository extends Repository<Integer, Contract> {
    List<Contract> findByProjectId(int projectId);
}
