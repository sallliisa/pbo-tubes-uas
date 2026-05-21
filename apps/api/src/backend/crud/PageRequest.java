package backend.crud;

import backend.model.ApiException;

public record PageRequest(int page, int limit) {
    public static PageRequest fromRaw(String pageRaw, String limitRaw) {
        Integer pageValue = parseNumeric(pageRaw, "page");
        Integer limitValue = parseNumeric(limitRaw, "limit");

        int page = pageValue == null ? 1 : Math.max(pageValue, 1);
        int limit = limitValue == null ? 10 : Math.max(limitValue, 1);
        return new PageRequest(page, limit);
    }

    private static Integer parseNumeric(String value, String field) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new ApiException("Query parameter \"" + field + "\" must be a number", 400);
        }
    }
}
