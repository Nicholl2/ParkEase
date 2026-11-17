package parkir.exception;

/** Input tidak valid: field kosong, tarif negatif, alasan batal kosong, dan sejenisnya. */
public class ValidasiException extends Exception {

    private static final long serialVersionUID = 1L;

    public ValidasiException(String pesan) {
        super(pesan);
    }
}
