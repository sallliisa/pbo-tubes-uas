package backend.crud;

import backend.model.ApiException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class CrudValidation {
    private CrudValidation() {}

    public static void validateAllowedFields(Map<String, Object> body, ModelAdapter adapter, boolean requireIdentity) {
        Set<String> allowed = new HashSet<>(adapter.writableFields());
        allowed.add(adapter.identityField());

        for (String key : body.keySet()) {
            if (!allowed.contains(key)) {
                throw new ApiException("Field \"" + key + "\" is not allowed for model \"" + adapter.modelName() + "\"", 400);
            }
        }

        if (requireIdentity && !body.containsKey(adapter.identityField())) {
            throw new ApiException(adapter.identityField() + " is required", 400);
        }
    }
}
