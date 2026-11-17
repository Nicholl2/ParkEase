package parkir.exception;

/** Pembungkus kegagalan operasi basis data. */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String pesan, Throwable penyebab) {
        super(pesan, penyebab);
    }

    public DataAccessException(String pesan) {
        super(pesan);
    }
}
