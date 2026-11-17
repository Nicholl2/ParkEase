package parkir.model;

import parkir.exception.ValidasiException;

/**
 * Kelas induk hierarki kendaraan.
 *
 * <p>Aturan tarif progresif ditulis sekali di {@link #hitungTarifParkir(int, int)},
 * sedangkan nilai yang berbeda per jenis (tarif dan denda) disediakan oleh
 * subclass. Dengan begitu pemanggilan lewat referensi {@code Kendaraan} tetap
 * menghasilkan angka berbeda untuk motor dan mobil.</p>
 */
public abstract class Kendaraan {

    private final long tarifJamPertama;
    private final long tarifJamBerikutnya;

    protected Kendaraan(long tarifJamPertama, long tarifJamBerikutnya) throws ValidasiException {
        if (tarifJamPertama < 0 || tarifJamBerikutnya < 0) {
            throw new ValidasiException("Tarif parkir tidak boleh negatif.");
        }
        this.tarifJamPertama = tarifJamPertama;
        this.tarifJamBerikutnya = tarifJamBerikutnya;
    }

    public abstract JenisKendaraan getJenis();

    public abstract long getDendaOverstay();

    public abstract long getDendaTiketHilang();

    public long getTarifJamPertama() {
        return tarifJamPertama;
    }

    public long getTarifJamBerikutnya() {
        return tarifJamBerikutnya;
    }

    /**
     * Tarif dasar parkir (belum termasuk denda).
     *
     * <p>Durasi dibulatkan ke atas per jam dan minimal satu jam. Setelah
     * {@code batasJamReguler}, tarif tidak bertambah lagi.</p>
     */
    public long hitungTarifParkir(int durasiJam, int batasJamReguler) {
        if (batasJamReguler < 1) {
            throw new IllegalArgumentException("Batas jam reguler minimal 1.");
        }
        int jamDitagih = Math.max(1, Math.min(durasiJam, batasJamReguler));
        if (jamDitagih == 1) {
            return tarifJamPertama;
        }
        return tarifJamPertama + (long) (jamDitagih - 1) * tarifJamBerikutnya;
    }

    public boolean isOverstay(int durasiJam, int batasJamReguler) {
        return durasiJam > batasJamReguler;
    }

    /** Factory sederhana: memilih subclass dari jenis kendaraan. */
    public static Kendaraan buat(JenisKendaraan jenis, long tarifJamPertama, long tarifJamBerikutnya)
            throws ValidasiException {
        if (jenis == null) {
            throw new ValidasiException("Jenis kendaraan wajib dipilih.");
        }
        switch (jenis) {
            case MOTOR:
                return new Motor(tarifJamPertama, tarifJamBerikutnya);
            case MOBIL:
                return new Mobil(tarifJamPertama, tarifJamBerikutnya);
            default:
                throw new ValidasiException("Jenis kendaraan tidak dikenal.");
        }
    }

    public static Kendaraan buat(JenisKendaraan jenis) throws ValidasiException {
        return buat(jenis, jenis == JenisKendaraan.MOTOR ? Motor.TARIF_JAM_PERTAMA : Mobil.TARIF_JAM_PERTAMA,
                jenis == JenisKendaraan.MOTOR ? Motor.TARIF_JAM_BERIKUTNYA : Mobil.TARIF_JAM_BERIKUTNYA);
    }
}
