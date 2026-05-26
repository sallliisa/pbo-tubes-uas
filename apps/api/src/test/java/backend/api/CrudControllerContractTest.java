package backend.api;

import backend.crud.CrudRegistry;
import backend.crud.GenericCrudService;
import backend.crud.ModelAdapter;
import backend.model.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CrudControllerContractTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        List<ModelAdapter> adapters = List.of(
            new StubAdapter("employees", "employee_id", List.of(
                "employee_id", "first_name", "last_name", "email", "hire_date", "salary", "employee_type", "benefit_plan", "annual_leave_quota"
            )),
            new StubAdapter("clients", "client_id", List.of(
                "client_id", "name", "industry", "contact_name", "contact_email", "contact_phone"
            )),
            new StubAdapter("departments", "department_id", List.of("department_id", "name")),
            new StubAdapter("invoices", "invoice_id", List.of("invoice_id", "title", "amount"))
        );

        CrudRegistry registry = new CrudRegistry(adapters);
        GenericCrudService service = new GenericCrudService(registry);
        CrudController controller = new CrudController(service, registry);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void listReturns200WithCanonicalEnvelopeAndMeta() throws Exception {
        mockMvc.perform(get("/api/employees/list?page=1&limit=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.data[0].employee_id").value(1001))
            .andExpect(jsonPath("$.meta.totalRecords").value(1))
            .andExpect(jsonPath("$.meta.totalPages").value(1))
            .andExpect(jsonPath("$.meta.currentPage").value(1))
            .andExpect(jsonPath("$.meta.limit").value(10));
    }

    @Test
    void listUsesDefaultsWhenPageAndLimitMissing() throws Exception {
        mockMvc.perform(get("/api/employees/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.meta.currentPage").value(1))
            .andExpect(jsonPath("$.meta.limit").value(10));
    }

    @Test
    void showByPathReturns200WithCanonicalEnvelope() throws Exception {
        mockMvc.perform(get("/api/employees/1001/show"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.data.employee_id").value(1001));
    }

    @Test
    void showByQueryReturns200WithCanonicalEnvelope() throws Exception {
        mockMvc.perform(get("/api/employees/show?id=1001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.data.employee_id").value(1001));
    }

    @Test
    void showMissingIdOnQueryReturns400CanonicalError() throws Exception {
        mockMvc.perform(get("/api/employees/show"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.ok").value(false))
            .andExpect(jsonPath("$.error.message").value("id is required"));
    }

    @Test
    void createReturns201AndCanonicalEnvelope() throws Exception {
        mockMvc.perform(post("/api/employees/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"employee_id\":1002,\"first_name\":\"A\",\"last_name\":\"B\",\"email\":\"a@b.com\",\"hire_date\":\"2026-01-01\",\"salary\":1000,\"employee_type\":\"Permanent Employee\",\"benefit_plan\":\"Gold\",\"annual_leave_quota\":10}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.data.employee_id").value(1002));
    }

    @Test
    void updateReturns200AndCanonicalEnvelope() throws Exception {
        mockMvc.perform(put("/api/clients/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"client_id\":7,\"name\":\"ACME\",\"industry\":\"Tech\",\"contact_name\":\"Jane\",\"contact_email\":\"jane@acme.com\",\"contact_phone\":\"0812\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.data.client_id").value(7));
    }

    @Test
    void updateAcceptsIdAliasForIdentity() throws Exception {
        mockMvc.perform(put("/api/clients/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":7,\"name\":\"ACME\",\"industry\":\"Tech\",\"contact_name\":\"Jane\",\"contact_email\":\"jane@acme.com\",\"contact_phone\":\"0812\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.data.client_id").value(7));
    }

    @Test
    void deleteReturns200AndCanonicalEnvelope() throws Exception {
        mockMvc.perform(delete("/api/departments/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"department_id\":3}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.data.department_id").value(3));
    }

    @Test
    void deleteAcceptsIdAliasForIdentity() throws Exception {
        mockMvc.perform(delete("/api/departments/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":3}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.data.department_id").value(3));
    }

    @Test
    void invoicesDeleteAcceptsIdAliasForIdentity() throws Exception {
        mockMvc.perform(delete("/api/invoices/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":11}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.data.invoice_id").value(11));
    }

    @Test
    void invalidPageReturns400CanonicalError() throws Exception {
        mockMvc.perform(get("/api/employees/list?page=abc&limit=10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.ok").value(false))
            .andExpect(jsonPath("$.error.message").value("Query parameter \"page\" must be a number"));
    }

    @Test
    void invalidLimitReturns400CanonicalError() throws Exception {
        mockMvc.perform(get("/api/employees/list?page=1&limit=nope"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.ok").value(false))
            .andExpect(jsonPath("$.error.message").value("Query parameter \"limit\" must be a number"));
    }

    @Test
    void unknownModelReturns400CanonicalError() throws Exception {
        mockMvc.perform(get("/api/unknown/list"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.ok").value(false))
            .andExpect(jsonPath("$.error.message").value("Invalid model"));
    }

    @Test
    void missingIdentityReturns400CanonicalErrorOnUpdate() throws Exception {
        mockMvc.perform(put("/api/clients/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"ACME\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.ok").value(false))
            .andExpect(jsonPath("$.error.message").value("client_id is required"));
    }

    @Test
    void unknownOrForbiddenFieldReturns400CanonicalError() throws Exception {
        mockMvc.perform(post("/api/departments/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"department_id\":1,\"name\":\"Finance\",\"unexpected\":\"x\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.ok").value(false))
            .andExpect(jsonPath("$.error.message").value("Field \"unexpected\" is not allowed for model \"departments\""));
    }

    private static final class StubAdapter implements ModelAdapter {
        private final String modelName;
        private final String identityField;
        private final List<String> writableFields;
        private final List<Map<String, Object>> store = new ArrayList<>();

        private StubAdapter(String modelName, String identityField, List<String> writableFields) {
            this.modelName = modelName;
            this.identityField = identityField;
            this.writableFields = writableFields;
            seed();
        }

        private void seed() {
            if ("employees".equals(modelName)) {
                store.add(new LinkedHashMap<>(Map.of("employee_id", 1001, "first_name", "Seed")));
            } else if ("invoices".equals(modelName)) {
                store.add(new LinkedHashMap<>(Map.of("invoice_id", 11, "title", "Seed Invoice", "amount", 1000)));
            }
        }

        @Override
        public List<Map<String, Object>> list() {
            return new ArrayList<>(store);
        }

        @Override
        public Map<String, Object> create(Map<String, Object> body) {
            Map<String, Object> created = new LinkedHashMap<>(body);
            store.add(created);
            return created;
        }

        @Override
        public Map<String, Object> update(Map<String, Object> body) {
            Object id = body.get(identityField);
            for (int i = 0; i < store.size(); i++) {
                if (java.util.Objects.equals(store.get(i).get(identityField), id)) {
                    Map<String, Object> merged = new LinkedHashMap<>(store.get(i));
                    merged.putAll(body);
                    store.set(i, merged);
                    return merged;
                }
            }
            if ("clients".equals(modelName)) {
                return new LinkedHashMap<>(body);
            }
            throw new ApiException("Record not found", 404);
        }

        @Override
        public Map<String, Object> delete(Map<String, Object> body) {
            Object id = body.get(identityField);
            for (int i = 0; i < store.size(); i++) {
                if (java.util.Objects.equals(store.get(i).get(identityField), id)) {
                    return store.remove(i);
                }
            }
            if ("departments".equals(modelName)) {
                return new LinkedHashMap<>(Map.of("department_id", id, "name", "Ops"));
            }
            throw new ApiException("Record not found", 404);
        }

        @Override
        public String modelName() {
            return modelName;
        }

        @Override
        public String identityField() {
            return identityField;
        }

        @Override
        public List<String> writableFields() {
            return writableFields;
        }
    }
}
