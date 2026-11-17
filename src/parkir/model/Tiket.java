package parkir.model;

import java.time.Duration;
import java.time.LocalDateTime;

import parkir.exception.ValidasiException;
import parkir.util.Jam;

/**
 * Sesi parkir. Menyimpan snapshot tarif saat masuk sehingga perubahan
 * pengaturan tarif tidak mengubah tagihan yang sedang berjalan.
 */
public class Tiket {

    private int id;
    private final String nomorTiket;
    private final SlotParkir slot;
    private final JenisKendaraan jenis;
    private final LocalDateTime waktuMasuk;

    private final long tarifJamPertamaSnapshot;
    private final long tarifJamBerikutnyaSnapshot;
    private final int batasJamRegulerSnapshot;
    private final long dendaOverstaySnapshot;
    private final long dendaTiketHilangSnapshot;

    private LocalDateTime waktuKeluar;
    private int durasiJam;
    private long dendaOverstay;
    private long dendaTiketHilang;
    private long total;
    private boolean jalurTiketHilang;
    private StatusTiket status = StatusTiket.AKTIF;
    private String alasanPembatalan;

    private int petugasMasukId;
    private String petugasMasukNama;
    private int petugasSelesaiId;
    private String petugasSelesaiNama;

    public Tiket(String nomorTiket, SlotParkir slot, JenisKendaraan jenis, LocalDateTime waktuMasuk,
            PengaturanTarif tarif, Petugas petugasMasuk) throws ValidasiException {
        this(nomorTiket, slot, jenis, waktuMasuk, tarif,
                petugasMasuk == null ? 0 : petugasMasuk.getId(),
                petugasMasuk == null ? null : petugasMasuk.getNama());
    }

    /** Overloading: dipakai DAO saat memuat tiket dari basis data. */
    public Tiket(String nomorTiket, SlotParkir slot, JenisKendaraan jenis, LocalDateTime waktuMasuk,
            PengaturanTarif tarif, int petugasMasukId, String petugasMasukNama) throws ValidasiException {
        if (nomorTiket == null || nomorTiket.isBlank()) {
            throw new ValidasiException("Nomor tiket wajib diisi.");
        }
        if (slot == null) {
            throw new ValidasiException("Slot wajib dipilih.");
        }
        if (tarif == null) {
            throw new ValidasiException("Pengaturan tarif belum tersedia.");
        }
        this.nomorTiket = nomorTiket;
        this.slot = slot;
        this.jenis = jenis;
        this.waktuMasuk = waktuMasuk;
        this.tarifJamPertamaSnapshot = tarif.getTarifJamPertama();
        this.tarifJamBerikutnyaSnapshot = tarif.getTarifJamBerikutnya();
        this.batasJamRegulerSnapshot = tarif.getBatasJamReguler();
        this.dendaOverstaySnapshot = tarif.getDendaOverstay();
        this.dendaTiketHilangSnapshot = tarif.getDendaTiketHilang();
        this.petugasMasukId = petugasMasukId;
        this.petugasMasukNama = petugasMasukNama;
    }

    /** Membulatkan durasi ke atas per jam, minimal satu jam. */
    public static int hitungDurasiJam(LocalDateTime masuk, LocalDateTime keluar) {
        long detik = Duration.between(masuk, keluar).getSeconds();
        if (detik <= 0) {
            return 1;
        }
        return (int) Math.max(1, (detik + 3599) / 3600);
    }

    /**
     * Menghitung rincian biaya memakai snapshot tarif dan flag yang tersimpan
     * pada tiket ini.
     */
    public RincianBiaya hitungRincian() {
        return hitungRincian(jalurTiketHilang);
    }

    /**
     * Overloading: menghitung rincian dengan menganggap tiket hilang atau tidak,
     * tanpa mengubah state tiket. Dipakai UI untuk pratinjau biaya.
     */
    public RincianBiaya hitungRincian(boolean anggapTiketHilang) {
        LocalDateTime akhir = waktuKeluar != null ? waktuKeluar : Jam.sekarang();
        int durasi = hitungDurasiJam(waktuMasuk, akhir);

        Kendaraan kendaraan;
        try {
            kendaraan = Kendaraan.buat(jenis, tarifJamPertamaSnapshot, tarifJamBerikutnyaSnapshot);
        } catch (ValidasiException e) {
            throw new IllegalStateException("Snapshot tarif pada tiket tidak valid", e);
        }

        long tarifDasar = kendaraan.hitungTarifParkir(durasi, batasJamRegulerSnapshot);
        boolean overstay = durasi > batasJamRegulerSnapshot;
        long dendaOver = overstay ? dendaOverstaySnapshot : 0L;
        long dendaHilang = anggapTiketHilang ? dendaTiketHilangSnapshot : 0L;

        return new RincianBiaya(durasi, tarifDasar, dendaOver, dendaHilang, overstay, anggapTiketHilang);
    }

    /** Menutup sesi parkir dan menyimpan hasil perhitungan ke tiket. */
    public RincianBiaya selesaikan(Petugas petugasSelesai) throws ValidasiException {
        if (status != StatusTiket.AKTIF) {
            throw new ValidasiException("Tiket " + nomorTiket + " tidak berstatus aktif.");
        }
        this.waktuKeluar = Jam.sekarang();
        RincianBiaya rincian = hitungRincian();
        this.durasiJam = rincian.getDurasiJam();
        this.dendaOverstay = rincian.getDendaOverstay();
        this.dendaTiketHilang = rincian.getDendaTiketHilang();
        this.total = rincian.getTotal();
        this.status = StatusTiket.SELESAI;
        this.petugasSelesaiId = petugasSelesai.getId();
        this.petugasSelesaiNama = petugasSelesai.getNama();
        return rincian;
    }

    /** Membatalkan tiket aktif tanpa menghapus jejaknya (soft delete). */
    public void batalkan(String alasan, Petugas oleh) throws ValidasiException {
        if (status != StatusTiket.AKTIF) {
            throw new ValidasiException("Hanya tiket aktif yang dapat dibatalkan.");
        }
        if (alasan == null || alasan.isBlank()) {
            throw new ValidasiException("Alasan pembatalan wajib diisi.");
        }
        this.alasanPembatalan = alasan.trim();
        this.status = StatusTiket.DIBATALKAN;
        this.waktuKeluar = Jam.sekarang();
        this.petugasSelesaiId = oleh.getId();
        this.petugasSelesaiNama = oleh.getNama();
    }

    public void tandaiTiketHilang() {
        this.jalurTiketHilang = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomorTiket() {
        return nomorTiket;
    }

    public SlotParkir getSlot() {
        return slot;
    }

    public JenisKendaraan getJenis() {
        return jenis;
    }

    public LocalDateTime getWaktuMasuk() {
        return waktuMasuk;
    }

    public LocalDateTime getWaktuKeluar() {
        return waktuKeluar;
    }

    public void setWaktuKeluar(LocalDateTime waktuKeluar) {
        this.waktuKeluar = waktuKeluar;
    }

    public int getDurasiJam() {
        return durasiJam;
    }

    public void setDurasiJam(int durasiJam) {
        this.durasiJam = durasiJam;
    }

    public long getDendaOverstay() {
        return dendaOverstay;
    }

    public void setDendaOverstay(long dendaOverstay) {
        this.dendaOverstay = dendaOverstay;
    }

    public long getDendaTiketHilang() {
        return dendaTiketHilang;
    }

    public void setDendaTiketHilang(long dendaTiketHilang) {
        this.dendaTiketHilang = dendaTiketHilang;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public boolean isJalurTiketHilang() {
        return jalurTiketHilang;
    }

    public void setJalurTiketHilang(boolean jalurTiketHilang) {
        this.jalurTiketHilang = jalurTiketHilang;
    }

    public StatusTiket getStatus() {
        return status;
    }

    public void setStatus(StatusTiket status) {
        this.status = status;
    }

    public String getAlasanPembatalan() {
        return alasanPembatalan;
    }

    public void setAlasanPembatalan(String alasanPembatalan) {
        this.alasanPembatalan = alasanPembatalan;
    }

    public int getPetugasMasukId() {
        return petugasMasukId;
    }

    public void setPetugasMasuk(int id, String nama) {
        this.petugasMasukId = id;
        this.petugasMasukNama = nama;
    }

    public String getPetugasMasukNama() {
        return petugasMasukNama;
    }

    public int getPetugasSelesaiId() {
        return petugasSelesaiId;
    }

    public void setPetugasSelesai(int id, String nama) {
        this.petugasSelesaiId = id;
        this.petugasSelesaiNama = nama;
    }

    public String getPetugasSelesaiNama() {
        return petugasSelesaiNama;
    }

    public long getTarifJamPertamaSnapshot() {
        return tarifJamPertamaSnapshot;
    }

    public long getTarifJamBerikutnyaSnapshot() {
        return tarifJamBerikutnyaSnapshot;
    }

    public int getBatasJamRegulerSnapshot() {
        return batasJamRegulerSnapshot;
    }

    public long getDendaOverstaySnapshot() {
        return dendaOverstaySnapshot;
    }

    public long getDendaTiketHilangSnapshot() {
        return dendaTiketHilangSnapshot;
    }

    @Override
    public String toString() {
        return nomorTiket + " - " + slot.getNomor() + " - " + jenis.getLabel() + " - " + status.getLabel();
    }
}
