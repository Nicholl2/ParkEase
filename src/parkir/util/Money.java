package parkir.util;

import java.text.NumberFormat;
import java.util.Locale;

import parkir.exception.ValidasiException;

/**
 * Utilitas format dan validasi nilai rupiah.
 *
 * <p>Nilai uang disimpan sebagai {@code long} rupiah (tanpa sen) agar sama
 * dengan kolom INTEGER di basis data.</p>
 */
public final class Money {

    private Money() {
    }

    /** Mengubah 18000 menjadi "Rp18.000". */
    public static String format(long rupiah) {
        NumberFormat format = NumberFormat.getInstance(Locale.of("id", "ID"));
        format.setMaximumFractionDigits(0);
        return "Rp" + format.format(rupiah);
    }

    /**
     * Memastikan nilai uang tidak negatif.
     *
     * @return nilai yang sama bila valid
     * @throws ValidasiException bila negatif
     */
    public static long validasi(long rupiah, String nama) throws ValidasiException {
        if (rupiah < 0) {
            throw new ValidasiException(nama + " tidak boleh negatif.");
        }
        return rupiah;
    }
}
