package parkir.exception;

/** Username tidak dikenal atau password salah. Pesannya sengaja tidak membedakan keduanya. */
public class AutentikasiGagalException extends Exception {

    private static final long serialVersionUID = 1L;

    public AutentikasiGagalException() {
        super("Username atau password salah.");
    }
}
