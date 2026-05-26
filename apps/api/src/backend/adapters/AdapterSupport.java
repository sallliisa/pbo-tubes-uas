package backend.adapters;

import backend.model.ApiException;
import domain.common.Identifiable;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.time.LocalDate;
import java.util.Map;

public final class AdapterSupport {
    private AdapterSupport() {}

    public static int requiredInt(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) throw new ApiException(key + " is required", 400);
        if (value instanceof Number n) return n.intValue();
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
            }
        }
        throw new ApiException(key + " must be a number", 400);
    }

    public static Integer optionalInt(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
            }
        }
        throw new ApiException(key + " must be a number", 400);
    }

    public static int nextIntId(List<? extends Identifiable<Integer>> entities) {
        return entities.stream()
            .map(Identifiable::getId)
            .filter(id -> id != null)
            .mapToInt(Integer::intValue)
            .max()
            .orElse(0) + 1;
    }

    public static String requiredString(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) throw new ApiException(key + " is required", 400);
        String out = String.valueOf(value).trim();
        if (out.isEmpty()) throw new ApiException(key + " is required", 400);
        return out;
    }

    public static String optionalString(Map<String, Object> body, String key, String fallback) {
        Object value = body.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    public static BigDecimal requiredDecimal(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) throw new ApiException(key + " is required", 400);
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception e) {
            throw new ApiException(key + " must be numeric", 400);
        }
    }

    public static LocalDate requiredDate(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) throw new ApiException(key + " is required", 400);
        try {
            return LocalDate.parse(String.valueOf(value));
        } catch (Exception e) {
            throw new ApiException(key + " must be date format yyyy-MM-dd", 400);
        }
    }

    public static Map<String, Object> toApiMap(Object target, Map<String, String> fieldAliases) {
        if (target == null) {
            return new LinkedHashMap<>();
        }
        return objectToMap(target, fieldAliases, new IdentityHashMap<>(), 0);
    }

    private static Map<String, Object> objectToMap(
        Object target,
        Map<String, String> fieldAliases,
        IdentityHashMap<Object, Boolean> seen,
        int depth
    ) {
        if (target == null) {
            return new LinkedHashMap<>();
        }
        if (seen.containsKey(target) || depth > 2) {
            return referenceMap(target, fieldAliases);
        }

        seen.put(target, Boolean.TRUE);
        Map<String, Object> out = new LinkedHashMap<>();

        for (Field field : instanceFields(target.getClass())) {
            try {
                field.setAccessible(true);
                out.put(apiName(field.getName(), fieldAliases), serializeValue(field.get(target), fieldAliases, seen, depth + 1));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to read field " + field.getName(), e);
            }
        }

        for (Method method : target.getClass().getMethods()) {
            String propertyName = propertyName(method);
            if (propertyName == null || "id".equals(propertyName)) {
                continue;
            }
            String apiName = apiName(propertyName, fieldAliases);
            if (out.containsKey(apiName)) {
                continue;
            }
            try {
                out.put(apiName, serializeValue(method.invoke(target), fieldAliases, seen, depth + 1));
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // Derived getters are optional for API completeness; skip unsafe ones.
            }
        }

        seen.remove(target);
        return out;
    }

    private static List<Field> instanceFields(Class<?> type) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            hierarchy.add(0, current);
            current = current.getSuperclass();
        }

        List<Field> fields = new ArrayList<>();
        for (Class<?> item : hierarchy) {
            for (Field field : item.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    private static Object serializeValue(
        Object value,
        Map<String, String> fieldAliases,
        IdentityHashMap<Object, Boolean> seen,
        int depth
    ) {
        if (value == null
            || value instanceof String
            || value instanceof Number
            || value instanceof Boolean
            || value instanceof TemporalAccessor) {
            return value;
        }
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                .map(item -> serializeValue(item, fieldAliases, seen, depth))
                .toList();
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                out.put(String.valueOf(entry.getKey()), serializeValue(entry.getValue(), fieldAliases, seen, depth));
            }
            return out;
        }
        if (value.getClass().getPackageName().startsWith("domain.")) {
            return objectToMap(value, fieldAliases, seen, depth);
        }
        return String.valueOf(value);
    }

    private static Map<String, Object> referenceMap(Object target, Map<String, String> fieldAliases) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (target instanceof Identifiable<?> identifiable) {
            String identityField = instanceFields(target.getClass()).stream()
                .map(Field::getName)
                .filter(name -> name.endsWith("Id"))
                .findFirst()
                .map(name -> apiName(name, fieldAliases))
                .orElse("id");
            out.put(identityField, identifiable.getId());
        }
        return out;
    }

    private static String propertyName(Method method) {
        if (method.getParameterCount() != 0 || method.getReturnType() == Void.TYPE) {
            return null;
        }
        String name = method.getName();
        if ("getClass".equals(name)) {
            return null;
        }
        if (name.startsWith("get") && name.length() > 3) {
            return decapitalize(name.substring(3));
        }
        if (name.startsWith("is") && name.length() > 2
            && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
            return decapitalize(name.substring(2));
        }
        return null;
    }

    private static String decapitalize(String value) {
        if (value.length() > 1 && Character.isUpperCase(value.charAt(0)) && Character.isUpperCase(value.charAt(1))) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private static String apiName(String javaName, Map<String, String> aliases) {
        return aliases.getOrDefault(javaName, camelToSnake(javaName));
    }

    private static String camelToSnake(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                out.append('_');
            }
            out.append(Character.toLowerCase(c));
        }
        return out.toString();
    }
}
