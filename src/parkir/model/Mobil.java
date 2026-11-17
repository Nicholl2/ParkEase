package parkir.model;

import parkir.exception.ValidasiException;

public final class Mobil extends Kendaraan {

    public static final long TARIF_JAM_PERTAMA = 10_000L;
    public static final long TARIF_JAM_BERIKUTNYA = 5_000L;
    public static final long DENDA_OVERSTAY = 50_000L;
    public static final long DENDA_TIKET_HILANG = 20_000L;

    public Mobil() throws ValidasiException {
        this(TARIF_JAM_PERTAMA, TARIF_JAM_BERIKUTNYA);
    }

    /** Overloading konstruktor: dipakai saat tarif diambil dari pengaturan di basis data. */
    public Mobil(long tarifJamPertama, long tarifJamBerikutnya) throws ValidasiException {
        super(tarifJamPertama, tarifJamBerikutnya);
    }

    @Override
    public JenisKendaraan getJenis() {
        return JenisKendaraan.MOBIL;
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
