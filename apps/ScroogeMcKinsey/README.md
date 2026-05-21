# ScroogeMcKinsey Generic CRUD Backend

This project exposes a `landing-hkr`-compatible open CRUD API on top of existing JDBC/MySQL repositories.

## API Contract

Base path:
- `/api/{model}/list`
- `/api/{model}/create`
- `/api/{model}/update`
- `/api/{model}/delete`

Supported models:
- `employees`
- `projects`
- `clients`
- `departments`
- `positions`

Success envelope:
```json
{ "ok": true, "data": {}, "meta": {} }
```

Error envelope:
```json
{ "ok": false, "error": { "message": "...", "code": null, "details": null } }
```

HTTP status rules:
- `list`: `200`
- `create`: `201`
- `update`: `200`
- `delete`: `200`

Pagination rules (`list`):
- Query params: `page`, `limit`
- Defaults: `page=1`, `limit=10`
- Invalid numeric format returns `400` with canonical error envelope.

## Environment Variables

Set these before run:
- `DB_URL` (default: `jdbc:mysql://localhost:3306/consulting_company_uas`)
- `DB_USER` (default: `root`)
- `DB_PASSWORD` (default: `gamer42069`)
- `SERVER_PORT` (optional Spring Boot port override)

Examples:
```bash
export DB_URL='jdbc:mysql://localhost:3306/consulting_company_uas'
export DB_USER='root'
export DB_PASSWORD='your_password'
export SERVER_PORT='8080'
```

## Run

Prerequisites:
- Java 17
- Maven
- MySQL with schema/tables used by existing JDBC repositories

Start server:
```bash
mvn spring-boot:run
```

## Curl Demo

### 1) List
```bash
curl -X GET 'http://localhost:8080/api/employees/list?page=1&limit=10'
```

Expected shape:
```json
{
  "ok": true,
  "data": [
    { "employee_id": 1001, "first_name": "..." }
  ],
  "meta": {
    "totalRecords": 1,
    "totalPages": 1,
    "currentPage": 1,
    "limit": 10
  }
}
```

### 2) Create
```bash
curl -X POST 'http://localhost:8080/api/departments/create' \
  -H 'Content-Type: application/json' \
  -d '{"department_id":91,"name":"Innovation"}'
```

### 3) Update
```bash
curl -X PUT 'http://localhost:8080/api/departments/update' \
  -H 'Content-Type: application/json' \
  -d '{"department_id":91,"name":"Innovation Lab"}'
```

### 4) Delete
```bash
curl -X DELETE 'http://localhost:8080/api/departments/delete' \
  -H 'Content-Type: application/json' \
  -d '{"department_id":91}'
```

## Failure Examples

Unknown model:
```bash
curl -X GET 'http://localhost:8080/api/unknown/list'
```

Expected:
```json
{
  "ok": false,
  "error": {
    "message": "Invalid model",
    "code": null,
    "details": null
  }
}
```

Invalid pagination:
```bash
curl -X GET 'http://localhost:8080/api/employees/list?page=a&limit=10'
```

Expected message:
```json
{ "ok": false, "error": { "message": "Query parameter \"page\" must be a number" } }
```

Missing identity on update/delete:
```bash
curl -X PUT 'http://localhost:8080/api/clients/update' \
  -H 'Content-Type: application/json' \
  -d '{"name":"ACME"}'
```

Expected message:
```json
{ "ok": false, "error": { "message": "client_id is required" } }
```

Unknown/forbidden field:
```bash
curl -X POST 'http://localhost:8080/api/departments/create' \
  -H 'Content-Type: application/json' \
  -d '{"department_id":92,"name":"Ops","unexpected":"x"}'
```

Expected message:
```json
{ "ok": false, "error": { "message": "Field \"unexpected\" is not allowed for model \"departments\"" } }
```

## Tests

Run contract tests:
```bash
mvn -Dtest=CrudControllerContractTest test
```

Optional MySQL smoke test (disabled by default):
```bash
export SCROOGE_RUN_MYSQL_TESTS=true
mvn -Dtest=MysqlIntegrationSmokeTest test
```

The MySQL smoke test checks JDBC connectivity and read operations for the core 5 repositories.
