package backend.crud;

import backend.model.ApiException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CrudRegistry {
    private final Map<String, ModelAdapter> adapters;

    public CrudRegistry(List<ModelAdapter> adapterList) {
        this.adapters = adapterList.stream().collect(Collectors.toMap(ModelAdapter::modelName, Function.identity()));
    }

    public ModelAdapter resolve(String model) {
        ModelAdapter adapter = adapters.get(model);
        if (adapter == null) {
            throw new ApiException("Invalid model", 400);
        }
        return adapter;
    }
}
