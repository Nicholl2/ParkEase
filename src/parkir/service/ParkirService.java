package parkir.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import parkir.dao.Database;
import parkir.dao.PembayaranDAO;
import parkir.dao.PengaturanTarifDAO;
import parkir.dao.SlotDAO;
import parkir.dao.TiketDAO;
import parkir.exception.DataAccessException;
import parkir.exception.SlotPenuhException;
import parkir.exception.ValidasiException;
import parkir.model.JenisKendaraan;
import parkir.model.MetodePembayaran;
import parkir.model.Pembayaran;
import parkir.model.PembayaranTunai;
import parkir.model.PengaturanTarif;
import parkir.model.Petugas;
import parkir.model.RincianBiaya;
import parkir.model.SlotParkir;
import parkir.model.StatusSlot;
import parkir.model.StatusTiket;
import parkir.model.Tiket;
import parkir.util.Jam;
import parkir.util.Money;
import parkir.util.NomorTiket;

/**
 * Alur bisnis utama parkir: kendaraan masuk, keluar, tiket hilang, pembatalan,
 * pencarian riwayat, dan unduh bukti transaksi.
 *
 * <p>Setiap operasi tulis berjalan dalam satu transaksi: koneksi dibuat di sini,
 * {@code autoCommit} dimatikan, lalu di-{@code commit} hanya bila seluruh
 * langkah berhasil. Bila ada kegagalan, semuanya di-{@code rollback} sehingga
 * status slot dan tiket tidak pernah setengah berubah.</p>
 */
public class ParkirService {

    private static final int MAKS_PERCOBAAN_NOMOR = 5;
    private static final DateTimeFormatter FORMAT_WAKTU = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final SlotDAO slotDAO = new SlotDAO();
    private final TiketDAO tiketDAO = new TiketDAO();
    private final PengaturanTarifDAO tarifDAO = new PengaturanTarifDAO();
    private final PembayaranDAO pembayaranDAO = new PembayaranDAO();
    private final AuditService audit = new AuditService();

    // ---------------------------------------------------------------- masuk

    /**
     * Mencatat kendaraan masuk.
     *
     * @param jenis      jenis kendaraan
     * @param nomorSlot  nomor slot yang dipilih; bila {@code null}/kosong sistem
     *                   memilih slot kosong pertama
     * @param petugas    petugas yang melayani
     * @throws SlotPenuhException bila tidak ada slot kosong untuk jenis tersebut
     */
    public Tiket masuk(JenisKendaraan jenis, String nomorSlot, Petugas petugas) throws ValidasiException {
        if (petugas == null) {
            throw new ValidasiException("Petugas wajib diketahui untuk mencatat kendaraan masuk.");
        }
        if (jenis == null) {
            throw new ValidasiException("Jenis kendaraan wajib dipilih.");
        }
        final LocalDateTime waktuMasuk = Jam.sekarang();

        return dalamTransaksi(koneksi -> {
            SlotParkir slot = pilihSlot(koneksi, jenis, nomorSlot);
            PengaturanTarif tarif = tarifDAO.ambil(koneksi, jenis);
            if (tarif == null) {
                throw new ValidasiException("Pengaturan tarif " + jenis.getLabel() + " belum tersedia.");
            }

            Tiket tiket = simpanTiketBaru(koneksi, slot, jenis, waktuMasuk, tarif, petugas);
            slotDAO.setStatus(koneksi, slot.getId(), StatusSlot.TERISI);
            audit.catat(koneksi, petugas, "KENDARAAN_MASUK", "tiket", tiket.getNomorTiket(),
                    jenis.getLabel() + " di slot " + slot.getNomor());
            return tiket;
        });
    }

    private SlotParkir pilihSlot(Connection koneksi, JenisKendaraan jenis, String nomorSlot)
            throws ValidasiException {
        if (nomorSlot == null || nomorSlot.isBlank()) {
            List<SlotParkir> kosong = slotDAO.berdasarkanStatus(koneksi, jenis, StatusSlot.KOSONG);
            if (kosong.isEmpty()) {
                throw new SlotPenuhException("Slot " + jenis.getLabel().toLowerCase()
                        + " penuh. Tidak ada slot kosong yang tersedia.");
            }
            return kosong.get(0);
        }

        SlotParkir slot = slotDAO.cariNomor(koneksi, nomorSlot);
        if (slot == null) {
            throw new ValidasiException("Slot " + nomorSlot + " tidak ditemukan.");
        }
        if (slot.getJenis() != jenis) {
            throw new ValidasiException("Slot " + slot.getNomor() + " khusus "
                    + slot.getJenis().getLabel().toLowerCase() + ".");
        }
        if (!slot.isKosong()) {
            throw new ValidasiException("Slot " + slot.getNomor() + " sedang terisi.");
        }
        return slot;
    }

    /**
     * Menyimpan tiket baru. Nomor tiket yang bentrok (UNIQUE) diulang sampai
     * {@value #MAKS_PERCOBAAN_NOMOR} kali sebelum kegagalan dilaporkan.
     */
    private Tiket simpanTiketBaru(Connection koneksi, SlotParkir slot, JenisKendaraan jenis,
            LocalDateTime waktuMasuk, PengaturanTarif tarif, Petugas petugas) throws ValidasiException {
        DataAccessException kegagalan = null;
        for (int percobaan = 1; percobaan <= MAKS_PERCOBAAN_NOMOR; percobaan++) {
            Tiket tiket = new Tiket(NomorTiket.baru(waktuMasuk), slot, jenis, waktuMasuk, tarif, petugas);
            try {
                int id = tiketDAO.simpan(koneksi, tiket);
                tiket.setId(id);
                return tiket;
            } catch (DataAccessException e) {
                if (!sebabUnik(e)) {
                    throw e;
                }
                kegagalan = e;
            }
        }
        throw new DataAccessException("Gagal membuat nomor tiket unik setelah "
                + MAKS_PERCOBAAN_NOMOR + " percobaan.", kegagalan);
    }

    private boolean sebabUnik(DataAccessException e) {
        Throwable sebab = e.getCause();
        String pesan = sebab == null ? e.getMessage() : sebab.getMessage();
        return pesan != null && pesan.toUpperCase().contains("UNIQUE");
    }

    // ---------------------------------------------------------------- keluar

    /**
     * Menutup sesi parkir: menghitung biaya, menyimpan pembayaran, mengosongkan
     * slot, dan mencatat audit.
     */
    public Pembayaran keluar(String nomorTiket, MetodePembayaran metode, Petugas petugas)
            throws ValidasiException {
        return selesaikan(nomorTiket, metode, petugas, false);
    }

    /** Varian tiket hilang: dikenakan denda tiket hilang sesuai snapshot. */
    public Pembayaran tiketHilang(String nomorTiket, MetodePembayaran metode, Petugas petugas)
            throws ValidasiException {
        return selesaikan(nomorTiket, metode, petugas, true);
    }

    private Pembayaran selesaikan(String nomorTiket, MetodePembayaran metode, Petugas petugas,
            boolean tiketHilang) throws ValidasiException {
        if (petugas == null) {
            throw new ValidasiException("Petugas wajib diketahui untuk memproses pembayaran.");
        }
        final MetodePembayaran metodeBayar = metode == null ? new PembayaranTunai() : metode;

        return dalamTransaksi(koneksi -> {
            Tiket tiket = tiketDAO.cariAktifByNomor(koneksi, nomorTiket);
            if (tiket == null) {
                throw new ValidasiException("Tiket " + nomorTiket + " tidak ditemukan atau sudah selesai.");
            }

            if (tiketHilang) {
                tiket.tandaiTiketHilang();
            }
            RincianBiaya rincian = tiket.selesaikan(petugas);
            String catatanMetode = metodeBayar.proses(rincian.getTotal());

            tiketDAO.perbarui(koneksi, tiket);
            slotDAO.setStatus(koneksi, tiket.getSlot().getId(), StatusSlot.KOSONG);

            LocalDateTime waktuBayar = Jam.sekarang();
            int pembayaranId = pembayaranDAO.simpan(koneksi, tiket.getId(), metodeBayar.getKode(),
                    rincian.getTotal(), waktuBayar.toString(), petugas.getId());

            audit.catat(koneksi, petugas, tiketHilang ? "TIKET_HILANG" : "KENDARAAN_KELUAR", "tiket",
                    tiket.getNomorTiket(),
                    Money.format(rincian.getTotal()) + " via " + metodeBayar.getLabel()
                            + (rincian.isOverstay() ? " (overstay)" : ""));

            return new Pembayaran(pembayaranId, tiket.getId(), metodeBayar.getKode(), rincian.getTotal(),
                    waktuBayar, petugas.getId(), petugas.getNama());
        });
    }

    // ------------------------------------------------------------ pembatalan

    /** Membatalkan tiket aktif (soft delete) dan mengosongkan slotnya. */
    public Tiket batalkan(String nomorTiket, String alasan, Petugas petugas) throws ValidasiException {
        if (petugas == null) {
            throw new ValidasiException("Petugas wajib diketahui untuk membatalkan tiket.");
        }
        return dalamTransaksi(koneksi -> {
            Tiket tiket = tiketDAO.cariAktifByNomor(koneksi, nomorTiket);
            if (tiket == null) {
                throw new ValidasiException("Tiket " + nomorTiket + " tidak ditemukan atau sudah selesai.");
            }
            tiket.batalkan(alasan, petugas);
            tiketDAO.perbarui(koneksi, tiket);
            slotDAO.setStatus(koneksi, tiket.getSlot().getId(), StatusSlot.KOSONG);
            audit.catat(koneksi, petugas, "BATAL_TIKET", "tiket", tiket.getNomorTiket(), alasan.trim());
            return tiket;
        });
    }

    // ------------------------------------------------------------- hitung

    /** Pratinjau biaya tiket aktif tanpa mengubah data. */
    public RincianBiaya hitungTotal(String nomorTiket) throws ValidasiException {
        return hitungTotal(nomorTiket, false);
    }

    /**
     * Overloading pratinjau biaya: {@code anggapTiketHilang} dipakai UI untuk
     * menampilkan dampak denda tiket hilang sebelum petugas memutuskan.
     */
    public RincianBiaya hitungTotal(String nomorTiket, boolean anggapTiketHilang) throws ValidasiException {
        try (Connection koneksi = Database.koneksi()) {
            Tiket tiket = tiketDAO.cariAktifByNomor(koneksi, nomorTiket);
            if (tiket == null) {
                throw new ValidasiException("Tiket " + nomorTiket + " tidak ditemukan atau sudah selesai.");
            }
            return tiket.hitungRincian(anggapTiketHilang);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------- bacaan

    public List<Tiket> daftarAktif() {
        try (Connection koneksi = Database.koneksi()) {
            return tiketDAO.daftarAktif(koneksi);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    public List<Tiket> cariRiwayat(String kataKunci, StatusTiket status, JenisKendaraan jenis) {
        try (Connection koneksi = Database.koneksi()) {
            return tiketDAO.cariRiwayat(koneksi, kataKunci, status, jenis);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    public Tiket cariAktif(String nomorTiket) {
        try (Connection koneksi = Database.koneksi()) {
            return tiketDAO.cariAktifByNomor(koneksi, nomorTiket);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    /** Rekap kapasitas: nilai {@code int[]{terisi, total}} per jenis. */
    public Map<JenisKendaraan, int[]> kapasitas() {
        try (Connection koneksi = Database.koneksi()) {
            return slotDAO.rekapKapasitas(koneksi);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    // --------------------------------------------------------------- bukti

    /**
     * Menulis bukti transaksi ke berkas teks. Operasi berkas dibungkus
     * try-catch agar kegagalan I/O tampil sebagai pesan yang bisa dibaca
     * pengguna, bukan stack trace.
     *
     * @return lokasi berkas yang ditulis
     */
    public Path unduhBukti(Tiket tiket, Path tujuan) throws ValidasiException {
        if (tiket == null) {
            throw new ValidasiException("Tiket wajib dipilih sebelum mengunduh bukti.");
        }
        if (tujuan == null) {
            throw new ValidasiException("Lokasi berkas bukti wajib diisi.");
        }
        try {
            Path induk = tujuan.toAbsolutePath().getParent();
            if (induk != null) {
                Files.createDirectories(induk);
            }
            Files.writeString(tujuan, susunBukti(tiket));
            return tujuan;
        } catch (IOException e) {
            throw new DataAccessException("Gagal menulis bukti transaksi: " + e.getMessage(), e);
        }
    }

    private String susunBukti(Tiket tiket) {
        RincianBiaya rincian = tiket.hitungRincian();
        StringBuilder teks = new StringBuilder();
        teks.append("========================================\n");
        teks.append("        BUKTI PARKIR TOTI CAKERY        \n");
        teks.append("========================================\n");
        teks.append("No. Tiket    : ").append(tiket.getNomorTiket()).append('\n');
        teks.append("Slot         : ").append(tiket.getSlot().getNomor()).append('\n');
        teks.append("Kendaraan    : ").append(tiket.getJenis().getLabel()).append('\n');
        teks.append("Masuk        : ").append(tiket.getWaktuMasuk().format(FORMAT_WAKTU)).append('\n');
        if (tiket.getWaktuKeluar() != null) {
            teks.append("Keluar       : ").append(tiket.getWaktuKeluar().format(FORMAT_WAKTU)).append('\n');
        }
        teks.append("Durasi       : ").append(rincian.getDurasiJam()).append(" jam\n");
        teks.append("----------------------------------------\n");
        teks.append("Tarif dasar  : ").append(Money.format(rincian.getTarifDasar())).append('\n');
        if (rincian.getDendaOverstay() > 0) {
            teks.append("Denda overstay: ").append(Money.format(rincian.getDendaOverstay())).append('\n');
        }
        if (rincian.getDendaTiketHilang() > 0) {
            teks.append("Denda hilang : ").append(Money.format(rincian.getDendaTiketHilang())).append('\n');
        }
        teks.append("TOTAL        : ").append(Money.format(rincian.getTotal())).append('\n');
        teks.append("----------------------------------------\n");
        teks.append("Petugas      : ").append(tiket.getPetugasMasukNama()).append('\n');
        teks.append("Status       : ").append(tiket.getStatus().getLabel()).append('\n');
        teks.append("Dicetak      : ").append(Jam.sekarang().format(FORMAT_WAKTU)).append('\n');
        return teks.toString();
    }

    // ----------------------------------------------------------- transaksi

    /** Satu unit kerja yang dijalankan di dalam transaksi. */
    @FunctionalInterface
    private interface Aksi<T> {
        T jalankan(Connection koneksi) throws ValidasiException;
    }

    private <T> T dalamTransaksi(Aksi<T> aksi) throws ValidasiException {
        try (Connection koneksi = Database.koneksi()) {
            koneksi.setAutoCommit(false);
            try {
                T hasil = aksi.jalankan(koneksi);
                koneksi.commit();
                return hasil;
            } catch (ValidasiException | RuntimeException e) {
                rollback(koneksi);
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Transaksi gagal: " + e.getMessage(), e);
        }
    }

    private void rollback(Connection koneksi) {
        try {
            koneksi.rollback();
        } catch (SQLException diabaikan) {
            // Rollback terbaik yang bisa dilakukan; kegagalan asli tetap dilempar.
        }
    }
}
