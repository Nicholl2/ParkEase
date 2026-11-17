package parkir.model;

import java.time.LocalDateTime;

import parkir.exception.ValidasiException;
import parkir.util.Jam;
import parkir.util.Money;

/**
 * Pengaturan tarif dan denda per jenis kendaraan, dapat diubah oleh petugas
 * dan dicatat di audit log. Nilainya di-snapshot ke tiket saat kendaraan masuk.
 */
public class PengaturanTarif {

    private final JenisKendaraan jenis;
    private long tarifJamPertama;
    private long tarifJamBerikutnya;
    private int batasJamReguler;
    private long dendaOverstay;
    private long dendaTiketHilang;
    private LocalDateTime berlakuSejak;

    public PengaturanTarif(JenisKendaraan jenis, long tarifJamPertama, long tarifJamBerikutnya,
            int batasJamReguler, long dendaOverstay, long dendaTiketHilang) throws ValidasiException {
        if (jenis == null) {
            throw new ValidasiException("Jenis kendaraan wajib dipilih.");
        }
        this.jenis = jenis;
        setTarifJamPertama(tarifJamPertama);
        setTarifJamBerikutnya(tarifJamBerikutnya);
        setBatasJamReguler(batasJamReguler);
        setDendaOverstay(dendaOverstay);
        setDendaTiketHilang(dendaTiketHilang);
        this.berlakuSejak = Jam.sekarang();
    }

    /** Nilai awal sesuai ketentuan tugas. */
    public static PengaturanTarif bawaan(JenisKendaraan jenis) throws ValidasiException {
        Kendaraan kendaraan = Kendaraan.buat(jenis);
        return new PengaturanTarif(jenis, kendaraan.getTarifJamPertama(), kendaraan.getTarifJamBerikutnya(),
                8, kendaraan.getDendaOverstay(), kendaraan.getDendaTiketHilang());
    }

    public JenisKendaraan getJenis() {
        return jenis;
    }

    public long getTarifJamPertama() {
        return tarifJamPertama;
    }

    public final void setTarifJamPertama(long nilai) throws ValidasiException {
        this.tarifJamPertama = Money.validasi(nilai, "Tarif jam pertama");
    }

    public long getTarifJamBerikutnya() {
        return tarifJamBerikutnya;
    }

    public final void setTarifJamBerikutnya(long nilai) throws ValidasiException {
        this.tarifJamBerikutnya = Money.validasi(nilai, "Tarif jam berikutnya");
    }

    public int getBatasJamReguler() {
        return batasJamReguler;
    }

    public final void setBatasJamReguler(int nilai) throws ValidasiException {
        if (nilai < 1) {
            throw new ValidasiException("Batas jam reguler minimal 1 jam.");
        }
        this.batasJamReguler = nilai;
    }

    public long getDendaOverstay() {
        return dendaOverstay;
    }

    public final void setDendaOverstay(long nilai) throws ValidasiException {
        this.dendaOverstay = Money.validasi(nilai, "Denda overstay");
    }

    public long getDendaTiketHilang() {
        return dendaTiketHilang;
    }

    public final void setDendaTiketHilang(long nilai) throws ValidasiException {
        this.dendaTiketHilang = Money.validasi(nilai, "Denda tiket hilang");
    }

    public LocalDateTime getBerlakuSejak() {
        return berlakuSejak;
    }

    public void setBerlakuSejak(LocalDateTime waktu) {
        this.berlakuSejak = waktu;
    }

    /** Mengubah pengaturan menjadi objek polimorfik untuk menghitung tarif. */
    public Kendaraan keKendaraan() throws ValidasiException {
        return Kendaraan.buat(jenis, tarifJamPertama, tarifJamBerikutnya);
    }

    @Override
    public String toString() {
        return jenis.getLabel() + ": " + Money.format(tarifJamPertama) + " / jam, berikutnya "
                + Money.format(tarifJamBerikutnya);
    }
}
