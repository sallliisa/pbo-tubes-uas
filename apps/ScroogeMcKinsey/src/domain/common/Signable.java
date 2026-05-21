package domain.common;

public interface Signable {
    void sign(String signer);

    boolean isSigned();
}
