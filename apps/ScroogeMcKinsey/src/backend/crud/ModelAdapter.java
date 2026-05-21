package backend.crud;

import java.util.List;
import java.util.Map;

public interface ModelAdapter {
    List<Map<String, Object>> list();

    Map<String, Object> create(Map<String, Object> body);

    Map<String, Object> update(Map<String, Object> body);

    Map<String, Object> delete(Map<String, Object> body);

    String modelName();

    String identityField();

    List<String> writableFields();
}
