package parkir.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Pembuat nomor tiket: {@code PKR-yyyyMMdd-XXXX}.
 *
 * <p>Empat karakter terakhir memakai alfabet tanpa huruf/angka yang mudah
 * tertukar (tanpa I, O, 0, 1). Keunikan tetap dijamin oleh constraint UNIQUE
 * di basis data; {@code ParkirService} mengulang pembuatan bila bentrok.</p>
 */
public final class NomorTiket {

    private static final char[] ABJAD = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final SecureRandom ACAK = new SecureRandom();
    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("yyyyMMdd");

    private NomorTiket() {
    }

    public static String baru(LocalDateTime waktu) {
        StringBuilder nomor = new StringBuilder("PKR-").append(waktu.format(TANGGAL)).append('-');
        for (int i = 0; i < 4; i++) {
            nomor.append(ABJAD[ACAK.nextInt(ABJAD.length)]);
        }
        return nomor.toString();
    }
}
