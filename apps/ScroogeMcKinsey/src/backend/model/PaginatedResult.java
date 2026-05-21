package backend.model;

import java.util.List;

public record PaginatedResult(List<?> data, int totalRecords, int totalPages, int currentPage, int limit) {
}
