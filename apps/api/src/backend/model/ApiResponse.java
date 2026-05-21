package backend.model;

import java.util.Map;

public class ApiResponse {
    private final boolean ok;
    private final Object data;
    private final Map<String, Object> meta;
    private final ApiError error;

    private ApiResponse(boolean ok, Object data, Map<String, Object> meta, ApiError error) {
        this.ok = ok;
        this.data = data;
        this.meta = meta;
        this.error = error;
    }

    public static ApiResponse success(Object data) {
        return new ApiResponse(true, data, null, null);
    }

    public static ApiResponse success(Object data, Map<String, Object> meta) {
        return new ApiResponse(true, data, meta, null);
    }

    public static ApiResponse failure(String message, String code, Object details) {
        return new ApiResponse(false, null, null, new ApiError(message, code, details));
    }

    public boolean isOk() {
        return ok;
    }

    public Object getData() {
        return data;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public ApiError getError() {
        return error;
    }
}
