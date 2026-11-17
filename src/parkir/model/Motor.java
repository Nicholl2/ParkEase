package parkir.model;

import parkir.exception.ValidasiException;

public final class Motor extends Kendaraan {

    public static final long TARIF_JAM_PERTAMA = 4_000L;
    public static final long TARIF_JAM_BERIKUTNYA = 2_000L;
    public static final long DENDA_OVERSTAY = 20_000L;
    public static final long DENDA_TIKET_HILANG = 10_000L;

    public Motor() throws ValidasiException {
        this(TARIF_JAM_PERTAMA, TARIF_JAM_BERIKUTNYA);
    }

    /** Overloading konstruktor: dipakai saat tarif diambil dari pengaturan di basis data. */
    public Motor(long tarifJamPertama, long tarifJamBerikutnya) throws ValidasiException {
        super(tarifJamPertama, tarifJamBerikutnya);
    }

    @Override
    public JenisKendaraan getJenis() {
        return JenisKendaraan.MOTOR;
    }

    @Override
    public long getDendaOverstay() {
        return DENDA_OVERSTAY;
    }

    @Override
    public long getDendaTiketHilang() {
        return DENDA_TIKET_HILANG;
    }
}
