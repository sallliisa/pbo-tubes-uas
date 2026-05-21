package exception;

import exceptions.DomainException;

public class InvalidContractStateException extends DomainException {
    public InvalidContractStateException(String message) {
        super(message);
    }
}
