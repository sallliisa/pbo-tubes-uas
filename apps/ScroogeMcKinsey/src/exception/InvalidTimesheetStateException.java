package exception;

import exceptions.DomainException;

public class InvalidTimesheetStateException extends DomainException {
    public InvalidTimesheetStateException(String message) {
        super(message);
    }
}
