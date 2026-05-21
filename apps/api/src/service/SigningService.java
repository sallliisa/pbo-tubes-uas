package service;

import java.util.List;

import domain.common.Signable;

public class SigningService {
    public void sign(Signable document, String signer) {
        document.sign(signer);
    }

    public void signAll(List<? extends Signable> documents, String signer) {
        for (Signable document : documents) {
            sign(document, signer);
        }
    }
}
