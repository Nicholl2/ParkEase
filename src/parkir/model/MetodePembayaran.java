package parkir.model;

import parkir.exception.ValidasiException;
import parkir.util.Money;

/**
 * Interface metode pembayaran (simulasi). Diimplementasikan lebih dari satu
 * kelas sehingga memenuhi ketentuan abstraksi dan polimorfisme.
 */
public interface MetodePembayaran {

    String getKode();

    String getLabel();

    /** Memproses pembayaran dan mengembalikan catatan untuk bukti transaksi. */
    String proses(long jumlah) throws ValidasiException;

    static MetodePembayaran dariKode(String kode) {
        if (kode == null) {
            return new PembayaranTunai();
        }
        for (MetodePembayaran metode : semua()) {
            if (metode.getKode().equalsIgnoreCase(kode)) {
                return metode;
            }
        }
        return new PembayaranTunai();
    }

    static MetodePembayaran[] semua() {
        return new MetodePembayaran[] { new PembayaranTunai(), new PembayaranQris() };
    }

    /** Validasi jumlah bayar yang dipakai bersama semua metode. */
    static long validasiJumlah(long jumlah) throws ValidasiException {
        if (jumlah <= 0) {
            throw new ValidasiException("Jumlah pembayaran harus lebih dari nol.");
        }
        return Money.validasi(jumlah, "Jumlah pembayaran");
    }
}
