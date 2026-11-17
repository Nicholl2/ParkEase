package parkir.util;

import java.time.LocalDateTime;

/**
 * Satu-satunya sumber waktu aplikasi.
 *
 * <p>Seluruh lapisan lain wajib memanggil {@link #sekarang()}, bukan
 * {@code LocalDateTime.now()}, supaya mode simulasi waktu (lihat
 * {@code ModeDemoPanel}) dapat memajukan jam sistem untuk mendemokan
 * overstay tanpa menunggu delapan jam.</p>
 *
 * <p>Untuk pengujian, {@link #setWaktuTetap(LocalDateTime)} membekukan waktu
 * sehingga durasi parkir dapat dipastikan tepat, misalnya delapan jam.</p>
 */
public final class Jam {

    private static int offsetJam = 0;
    private static LocalDateTime waktuTetap = null;

    private Jam() {
    }

    public static LocalDateTime sekarang() {
        if (waktuTetap != null) {
            return waktuTetap.plusHours(offsetJam);
        }
        return LocalDateTime.now().plusHours(offsetJam);
    }

    public static int getOffsetJam() {
        return offsetJam;
    }

    /** Memajukan/memundurkan waktu aplikasi sebesar {@code jam} jam. */
    public static void setOffsetJam(int jam) {
        offsetJam = jam;
    }

    /** Membekukan waktu pada nilai tertentu; {@code null} berarti kembali ke jam sistem. */
    public static void setWaktuTetap(LocalDateTime waktu) {
        waktuTetap = waktu;
    }

    public static boolean isWaktuTetap() {
        return waktuTetap != null;
    }

    public static void reset() {
        offsetJam = 0;
        waktuTetap = null;
    }
}
