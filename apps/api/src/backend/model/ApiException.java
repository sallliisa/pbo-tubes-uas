package backend.model;

public class ApiException extends RuntimeException {
    private final int status;
    private final String code;
    private final Object details;

    public ApiException(String message, int status) {
        this(message, status, null, null);
    }

    public ApiException(String message, int status, String code, Object details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public Object getDetails() {
        return details;
    }
}
