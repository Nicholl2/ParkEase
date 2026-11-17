package parkir.exception;

/** Tidak ada slot kosong untuk jenis kendaraan yang diminta. */
public class SlotPenuhException extends ValidasiException {

    private static final long serialVersionUID = 1L;

    public SlotPenuhException(String pesan) {
        super(pesan);
    }
}
