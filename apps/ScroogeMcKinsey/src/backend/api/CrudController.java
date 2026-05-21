package backend.api;

import backend.crud.CrudValidation;
import backend.crud.GenericCrudService;
import backend.crud.ModelAdapter;
import backend.crud.PageRequest;
import backend.crud.CrudRegistry;
import backend.model.ApiException;
import backend.model.ApiResponse;
import backend.model.PaginatedResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/{model}")
public class CrudController {
    private final GenericCrudService crudService;
    private final CrudRegistry registry;

    public CrudController(GenericCrudService crudService, CrudRegistry registry) {
        this.crudService = crudService;
        this.registry = registry;
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> list(
        @PathVariable String model,
        @RequestParam(required = false) String page,
        @RequestParam(required = false) String limit
    ) {
        PaginatedResult result = crudService.list(model, PageRequest.fromRaw(page, limit));
        return ResponseEntity.ok(ApiResponse.success(result.data(), Map.of(
            "totalRecords", result.totalRecords(),
            "totalPages", result.totalPages(),
            "currentPage", result.currentPage(),
            "limit", result.limit()
        )));
    }

    @GetMapping("/{id}/show")
    public ResponseEntity<ApiResponse> showByPath(
        @PathVariable("model") String model,
        @PathVariable("id") String id
    ) {
        return ResponseEntity.ok(ApiResponse.success(resolveRecordById(model, id)));
    }

    @GetMapping("/show")
    public ResponseEntity<ApiResponse> showByQuery(
        @PathVariable("model") String model,
        @RequestParam(name = "id", required = false) String id
    ) {
        if (id == null || id.isBlank()) {
            throw new ApiException("id is required", 400);
        }
        return ResponseEntity.ok(ApiResponse.success(resolveRecordById(model, id)));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> create(@PathVariable String model, @RequestBody Map<String, Object> body) {
        ModelAdapter adapter = registry.resolve(model);
        CrudValidation.validateAllowedFields(body, adapter, false);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(crudService.create(model, body)));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse> update(@PathVariable String model, @RequestBody Map<String, Object> body) {
        ModelAdapter adapter = registry.resolve(model);
        CrudValidation.validateAllowedFields(body, adapter, true);
        return ResponseEntity.ok(ApiResponse.success(crudService.update(model, body)));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> delete(@PathVariable String model, @RequestBody Map<String, Object> body) {
        ModelAdapter adapter = registry.resolve(model);
        CrudValidation.validateAllowedFields(body, adapter, true);
        return ResponseEntity.ok(ApiResponse.success(crudService.delete(model, body)));
    }

    private Map<String, Object> resolveRecordById(String model, String id) {
        ModelAdapter adapter = registry.resolve(model);
        String identityField = adapter.identityField();
        for (Map<String, Object> row : adapter.list()) {
            Object value = row.get(identityField);
            if (Objects.equals(String.valueOf(value), id)) {
                return row;
            }
        }

        Map<String, Object> details = new LinkedHashMap<>();
        details.put(identityField, id);
        throw new ApiException("Record not found", 404, null, details);
    }
}
