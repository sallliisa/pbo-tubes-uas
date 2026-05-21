package persistence.jdbc;

public class JdbcRepositoryException extends RuntimeException {
    public JdbcRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
