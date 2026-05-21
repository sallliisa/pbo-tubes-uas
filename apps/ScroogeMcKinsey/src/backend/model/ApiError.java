package backend.model;

public class ApiError {
    private final String message;
    private final String code;
    private final Object details;

    public ApiError(String message, String code, Object details) {
        this.message = message;
        this.code = code;
        this.details = details;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public Object getDetails() {
        return details;
    }
}
