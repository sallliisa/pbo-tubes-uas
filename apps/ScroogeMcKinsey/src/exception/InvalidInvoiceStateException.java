package exception;

import exceptions.DomainException;

public class InvalidInvoiceStateException extends DomainException {
    public InvalidInvoiceStateException(String message) {
        super(message);
    }
}
