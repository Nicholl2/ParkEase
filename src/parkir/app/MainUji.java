package parkir.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import parkir.dao.Database;
import parkir.exception.AutentikasiGagalException;
import parkir.exception.DataAccessException;
import parkir.exception.SlotPenuhException;
import parkir.exception.ValidasiException;
import parkir.model.JenisKendaraan;
import parkir.model.Kendaraan;
import parkir.model.MetodePembayaran;
import parkir.model.Mobil;
import parkir.model.Motor;
import parkir.model.Pembayaran;
import parkir.model.PengaturanTarif;
import parkir.model.Petugas;
import parkir.model.RincianBiaya;
import parkir.model.SlotParkir;
import parkir.model.StatusTiket;
import parkir.model.Tiket;
import parkir.service.AuditService;
import parkir.service.AuthService;
import parkir.service.ParkirService;
import parkir.service.PetugasService;
import parkir.service.SlotService;
import parkir.service.TarifService;
import parkir.util.Jam;
import parkir.util.Money;

/**
 * Harness uji konsol untuk lapisan model, DAO, dan service.
 *
 * <p>Dijalankan lewat {@code scripts/run-console.sh}. Semua pemeriksaan memakai
 * basis data sementara di {@code data/}, bukan data aplikasi sehari-hari.</p>
 */
public final class MainUji {

    private static int lulus = 0;
    private static int gagal = 0;

    private static final Path DB_UJI = Path.of("data", "uji-main.db");
    private static final Path DB_PENUH = Path.of("data", "uji-penuh.db");

    private static final AuthService auth = new AuthService();
    private static final SlotService slotService = new SlotService();
    private static final PetugasService petugasService = new PetugasService();
    private static final TarifService tarifService = new TarifService();
    private static final ParkirService parkir = new ParkirService();
    private static final AuditService auditService = new AuditService();

    private MainUji() {
    }

    public static void main(String[] args) throws Exception {
        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 5, 8, 0));

        siapkanBasisData(DB_UJI);
        Petugas admin = ujiLogin();
        ujiPolimorfismeDanInterface();
        ujiCrudSlot(admin);
        ujiCrudPetugas(admin);
        ujiAlurParkirMotor(admin);
        ujiAlurParkirMobil(admin);
        ujiOverstayDanTiketHilang(admin);
        ujiPembatalan(admin);
        ujiSnapshotTarif(admin);
        ujiPencarianKapasitasDanBukti(admin);
        ujiSlotPenuh(admin);
        ujiPersistensi();

        Jam.reset();
        System.out.println();
        System.out.println("========================================");
        System.out.println("LULUS: " + lulus + "   GAGAL: " + gagal);
        System.out.println("========================================");
        if (gagal > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------- skenario

    private static void siapkanBasisData(Path lokasi) throws Exception {
        Files.deleteIfExists(lokasi);
        Database.aturLokasi(lokasi.toString());
        Database.inisialisasi();

        cek("seed 1 petugas", petugasService.semua().size() == 1);
        cek("seed 150 slot", slotService.semua().size() == 150);
        cek("seed 2 pengaturan tarif", tarifService.semua().size() == 2);

        Map<JenisKendaraan, int[]> rekap = slotService.rekapKapasitas();
        cek("kapasitas motor 120", rekap.get(JenisKendaraan.MOTOR)[1] == 120);
        cek("kapasitas mobil 30", rekap.get(JenisKendaraan.MOBIL)[1] == 30);
    }

    private static Petugas ujiLogin() throws Exception {
        Petugas admin = auth.login("admin", "admin123");
        cek("login benar mengembalikan admin", admin != null && "admin".equals(admin.getUsername()));

        cekGagal("login password salah ditolak", AutentikasiGagalException.class,
                () -> auth.login("admin", "salah"));
        cekGagal("login username tak dikenal ditolak", AutentikasiGagalException.class,
                () -> auth.login("tidakada", "admin123"));
        return admin;
    }

    private static void ujiPolimorfismeDanInterface() throws Exception {
        Kendaraan motor = new Motor();
        Kendaraan mobil = new Mobil();

        cek("polimorfisme: motor", motor.getJenis() == JenisKendaraan.MOTOR);
        cek("polimorfisme: mobil", mobil.getJenis() == JenisKendaraan.MOBIL);
        cek("tarif motor 3 jam = 8.000", motor.hitungTarifParkir(3, 8) == 8_000L);
        cek("tarif mobil 3 jam = 20.000", mobil.hitungTarifParkir(3, 8) == 20_000L);
        cek("denda overstay beda per subclass", motor.getDendaOverstay() != mobil.getDendaOverstay());

        MetodePembayaran[] semua = MetodePembayaran.semua();
        cek("dua implementasi MetodePembayaran", semua.length == 2);
        cek("kode TUNAI", "TUNAI".equals(semua[0].getKode()));
        cek("kode QRIS", "QRIS".equals(semua[1].getKode()));
        cek("proses tunai", semua[0].proses(25_000L).contains(Money.format(25_000L)));
        cek("proses qris", semua[1].proses(10_000L).toLowerCase().contains("qris"));
        cekGagal("pembayaran nol ditolak", ValidasiException.class, () -> semua[0].proses(0L));

        cek("Money.format 18000", "Rp18.000".equals(Money.format(18_000L)));
        cekGagal("uang negatif ditolak", ValidasiException.class, () -> Money.validasi(-1, "Total"));
    }

    private static void ujiCrudSlot(Petugas admin) throws Exception {
        SlotParkir baru = slotService.tambah("t-900", JenisKendaraan.MOTOR, admin);
        cek("tambah slot menormalkan nomor", "T-900".equals(baru.getNomor()));
        cek("slot baru berstatus kosong", baru.isKosong());

        cekGagal("nomor slot duplikat ditolak", ValidasiException.class,
                () -> slotService.tambah("T-900", JenisKendaraan.MOTOR, admin));

        slotService.ubah(baru.getId(), "T-901", JenisKendaraan.MOTOR, admin);
        boolean ketemu = slotService.semua().stream()
                .anyMatch(s -> s.getId() == baru.getId() && "T-901".equals(s.getNomor()));
        cek("ubah slot tersimpan", ketemu);

        slotService.hapus(baru.getId(), admin);
        boolean hilang = slotService.semua().stream().noneMatch(s -> s.getId() == baru.getId());
        cek("hapus slot berhasil", hilang);
    }

    private static void ujiCrudPetugas(Petugas admin) throws Exception {
        Petugas baru = petugasService.tambah("petugas2", "Petugas Dua", "rahasia123", admin);
        cek("tambah petugas", baru != null && baru.getId() > 0);
        cek("password petugas ter-hash", !"rahasia123".equals(baru.getPasswordHash()));

        cekGagal("username petugas duplikat ditolak", ValidasiException.class,
                () -> petugasService.tambah("petugas2", "Kembar", "rahasia123", admin));
        cekGagal("password pendek ditolak", ValidasiException.class,
                () -> petugasService.tambah("petugas3", "Pendek", "123", admin));

        petugasService.ubah(baru.getId(), "petugas2", "Petugas Dua Edit", admin);
        cek("login setelah ubah nama", auth.login("petugas2", "rahasia123").getNama().equals("Petugas Dua Edit"));

        petugasService.resetPassword(baru.getId(), "baru12345", admin);
        cek("login dengan password baru", auth.login("petugas2", "baru12345") != null);
        cekGagal("password lama tidak berlaku", AutentikasiGagalException.class,
                () -> auth.login("petugas2", "rahasia123"));

        cekGagal("hapus akun sendiri ditolak", ValidasiException.class,
                () -> petugasService.hapus(admin.getId(), admin));

        petugasService.hapus(baru.getId(), admin);
        cekGagal("petugas terhapus tidak bisa login", AutentikasiGagalException.class,
                () -> auth.login("petugas2", "baru12345"));
    }

    private static void ujiAlurParkirMotor(Petugas admin) throws Exception {
        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 5, 8, 0));
        Tiket tiket = parkir.masuk(JenisKendaraan.MOTOR, "M-001", admin);
        cek("masuk: nomor tiket berformat", tiket.getNomorTiket().matches("PKR-\\d{8}-[A-Z0-9]{4}"));
        cek("masuk: status aktif", tiket.getStatus() == StatusTiket.AKTIF);
        cek("masuk: slot jadi terisi", slotTerisi("M-001"));

        cekGagal("slot terisi tidak bisa dipakai lagi", ValidasiException.class,
                () -> parkir.masuk(JenisKendaraan.MOTOR, "M-001", admin));
        cekGagal("slot motor tidak untuk mobil", ValidasiException.class,
                () -> parkir.masuk(JenisKendaraan.MOBIL, "M-002", admin));
        cekGagal("slot tak dikenal ditolak", ValidasiException.class,
                () -> parkir.masuk(JenisKendaraan.MOTOR, "X-999", admin));

        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 5, 16, 0));
        RincianBiaya rincian = parkir.hitungTotal(tiket.getNomorTiket());
        cek("motor 8 jam: durasi 8", rincian.getDurasiJam() == 8);
        cek("motor 8 jam: tarif 18.000", rincian.getTarifDasar() == 18_000L);
        cek("motor 8 jam: tanpa denda", rincian.getTotal() == 18_000L && !rincian.isOverstay());

        Pembayaran bayar = parkir.keluar(tiket.getNomorTiket(), MetodePembayaran.dariKode("TUNAI"), admin);
        cek("keluar: jumlah 18.000", bayar.getJumlah() == 18_000L);
        cek("keluar: metode TUNAI", "TUNAI".equals(bayar.getMetode()));
        cek("keluar: slot kembali kosong", slotKosong("M-001"));

        Tiket tersimpan = parkir.cariRiwayat(tiket.getNomorTiket(), null, null).get(0);
        cek("keluar: status SELESAI", tersimpan.getStatus() == StatusTiket.SELESAI);
        cek("keluar: total tersimpan", tersimpan.getTotal() == 18_000L);
        cek("keluar: petugas selesai tercatat", tersimpan.getPetugasSelesaiId() == admin.getId());

        cekGagal("tiket selesai tidak bisa dibayar ulang", ValidasiException.class,
                () -> parkir.keluar(tiket.getNomorTiket(), MetodePembayaran.dariKode("QRIS"), admin));
    }

    private static void ujiAlurParkirMobil(Petugas admin) throws Exception {
        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 6, 8, 0));
        Tiket tiket = parkir.masuk(JenisKendaraan.MOBIL, null, admin);
        cek("masuk mobil: slot otomatis C-001", "C-001".equals(tiket.getSlot().getNomor()));

        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 6, 12, 0));
        RincianBiaya rincian = parkir.hitungTotal(tiket.getNomorTiket());
        cek("mobil 4 jam: tarif 25.000", rincian.getTotal() == 25_000L);

        Pembayaran bayar = parkir.keluar(tiket.getNomorTiket(), MetodePembayaran.dariKode("QRIS"), admin);
        cek("mobil bayar QRIS 25.000", bayar.getJumlah() == 25_000L && "QRIS".equals(bayar.getMetode()));
        cek("slot mobil kembali kosong", slotKosong("C-001"));
    }

    private static void ujiOverstayDanTiketHilang(Petugas admin) throws Exception {
        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 7, 8, 0));
        Tiket motor = parkir.masuk(JenisKendaraan.MOTOR, null, admin);
        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 7, 18, 0));

        RincianBiaya overstay = parkir.hitungTotal(motor.getNomorTiket());
        cek("overstay motor: durasi 10", overstay.getDurasiJam() == 10);
        cek("overstay motor: tarif dibatasi 18.000", overstay.getTarifDasar() == 18_000L);
        cek("overstay motor: total 38.000", overstay.getTotal() == 38_000L && overstay.isOverstay());

        Pembayaran bayarOverstay = parkir.keluar(motor.getNomorTiket(),
                MetodePembayaran.dariKode("TUNAI"), admin);
        cek("overstay motor dibayar 38.000", bayarOverstay.getJumlah() == 38_000L);

        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 8, 8, 0));
        Tiket mobil = parkir.masuk(JenisKendaraan.MOBIL, "C-002", admin);
        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 8, 18, 0));

        RincianBiaya hilang = parkir.hitungTotal(mobil.getNomorTiket(), true);
        cek("tiket hilang mobil: total 115.000", hilang.getTotal() == 115_000L && hilang.isTiketHilang());

        Pembayaran bayarHilang = parkir.tiketHilang(mobil.getNomorTiket(),
                MetodePembayaran.dariKode("TUNAI"), admin);
        cek("tiket hilang dibayar 115.000", bayarHilang.getJumlah() == 115_000L);

        Tiket tersimpan = parkir.cariRiwayat(mobil.getNomorTiket(), null, null).get(0);
        cek("tiket hilang: flag tersimpan", tersimpan.isJalurTiketHilang());
        cek("tiket hilang: denda tersimpan", tersimpan.getDendaTiketHilang() == 20_000L);
    }

    private static void ujiPembatalan(Petugas admin) throws Exception {
        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 9, 8, 0));
        Tiket tiket = parkir.masuk(JenisKendaraan.MOTOR, null, admin);

        cekGagal("batal tanpa alasan ditolak", ValidasiException.class,
                () -> parkir.batalkan(tiket.getNomorTiket(), "   ", admin));

        parkir.batalkan(tiket.getNomorTiket(), "Salah input plat", admin);
        Tiket tersimpan = parkir.cariRiwayat(tiket.getNomorTiket(), null, null).get(0);
        cek("batal: status DIBATALKAN", tersimpan.getStatus() == StatusTiket.DIBATALKAN);
        cek("batal: alasan tersimpan", "Salah input plat".equals(tersimpan.getAlasanPembatalan()));
        cek("batal: slot dikosongkan", slotKosong(tersimpan.getSlot().getNomor()));
        cekGagal("batal dua kali ditolak", ValidasiException.class,
                () -> parkir.batalkan(tiket.getNomorTiket(), "Lagi", admin));
        cekGagal("tiket batal tidak bisa dibayar", ValidasiException.class,
                () -> parkir.keluar(tiket.getNomorTiket(), MetodePembayaran.dariKode("TUNAI"), admin));
    }

    private static void ujiSnapshotTarif(Petugas admin) throws Exception {
        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 10, 8, 0));
        Tiket lama = parkir.masuk(JenisKendaraan.MOTOR, null, admin);

        tarifService.simpan(new PengaturanTarif(JenisKendaraan.MOTOR, 5_000L, 3_000L, 8, 25_000L, 15_000L),
                admin);

        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 10, 9, 0));
        RincianBiaya rincianLama = parkir.hitungTotal(lama.getNomorTiket());
        cek("snapshot: tiket lama tetap 4.000", rincianLama.getTotal() == 4_000L);
        cek("snapshot: denda overstay lama 20.000", lama.getDendaOverstaySnapshot() == 20_000L);

        Tiket baru = parkir.masuk(JenisKendaraan.MOTOR, null, admin);
        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 10, 10, 0));
        RincianBiaya rincianBaru = parkir.hitungTotal(baru.getNomorTiket());
        cek("tarif baru berlaku untuk tiket baru", rincianBaru.getTotal() == 5_000L);

        Pembayaran bayarLama = parkir.keluar(lama.getNomorTiket(), MetodePembayaran.dariKode("TUNAI"), admin);
        Pembayaran bayarBaru = parkir.keluar(baru.getNomorTiket(), MetodePembayaran.dariKode("TUNAI"), admin);
        // Tiket lama keluar pukul 10.00 (2 jam) memakai tarif lama: 4.000 + 2.000.
        cek("snapshot: pembayaran lama 6.000", bayarLama.getJumlah() == 6_000L);
        cek("snapshot: pembayaran baru 5.000", bayarBaru.getJumlah() == 5_000L);

        tarifService.simpan(PengaturanTarif.bawaan(JenisKendaraan.MOTOR), admin);
        cek("tarif motor kembali 4.000", tarifService.ambil(JenisKendaraan.MOTOR).getTarifJamPertama() == 4_000L);
    }

    private static void ujiPencarianKapasitasDanBukti(Petugas admin) throws Exception {
        List<Tiket> semua = parkir.cariRiwayat("PKR-", null, null);
        cek("riwayat: ada data", !semua.isEmpty());

        List<Tiket> selesaiMobil = parkir.cariRiwayat(null, StatusTiket.SELESAI, JenisKendaraan.MOBIL);
        cek("riwayat: filter status + jenis", !selesaiMobil.isEmpty()
                && selesaiMobil.stream().allMatch(t -> t.getStatus() == StatusTiket.SELESAI
                        && t.getJenis() == JenisKendaraan.MOBIL));

        cek("riwayat: kata kunci tak ada hasil", parkir.cariRiwayat("ZZZZ", null, null).isEmpty());

        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 11, 8, 0));
        Tiket aktif = parkir.masuk(JenisKendaraan.MOTOR, null, admin);
        cek("daftar aktif berisi tiket baru", parkir.daftarAktif().stream()
                .anyMatch(t -> t.getNomorTiket().equals(aktif.getNomorTiket())));
        cek("cari aktif by nomor", parkir.cariAktif(aktif.getNomorTiket()) != null);

        Map<JenisKendaraan, int[]> rekap = parkir.kapasitas();
        cek("kapasitas motor terisi 1", rekap.get(JenisKendaraan.MOTOR)[0] == 1);
        cek("kapasitas mobil terisi 0", rekap.get(JenisKendaraan.MOBIL)[0] == 0);

        Jam.setWaktuTetap(LocalDateTime.of(2026, 1, 11, 9, 0));
        Path bukti = Path.of("data", "uji-bukti.txt");
        parkir.unduhBukti(aktif, bukti);
        String isi = Files.readString(bukti);
        cek("bukti memuat nomor tiket", isi.contains(aktif.getNomorTiket()));
        cek("bukti memuat total", isi.contains("TOTAL") && isi.contains(Money.format(4_000L)));
        cekGagal("unduh bukti tanpa tiket ditolak", ValidasiException.class,
                () -> parkir.unduhBukti(null, bukti));

        parkir.keluar(aktif.getNomorTiket(), MetodePembayaran.dariKode("TUNAI"), admin);
        Files.deleteIfExists(bukti);

        cek("audit log tercatat", auditService.terbaru(50).stream()
                .anyMatch(a -> "KENDARAAN_MASUK".equals(a.getAksi())));
        cek("audit mencatat login gagal", auditService.terbaru(100).stream()
                .anyMatch(a -> "LOGIN_GAGAL".equals(a.getAksi())));
        cek("audit mencatat ubah tarif", auditService.terbaru(100).stream()
                .anyMatch(a -> "UBAH_TARIF".equals(a.getAksi())));

        Petugas sementara = petugasService.tambah("petugas4", "Petugas Sementara", "sementara123", admin);
        auth.login("petugas4", "sementara123");
        cekGagal("hapus petugas dengan riwayat ditolak", ValidasiException.class,
                () -> petugasService.hapus(admin.getId(), sementara));
        petugasService.hapus(sementara.getId(), admin);
        cek("hapus petugas tanpa riwayat berhasil", petugasService.semua().stream()
                .noneMatch(p -> p.getId() == sementara.getId()));
        cek("audit tetap menyimpan nama petugas terhapus", auditService.terbaru(200).stream()
                .anyMatch(a -> "Petugas Sementara".equals(a.getPetugasNama())));
    }

    private static void ujiSlotPenuh(Petugas admin) throws Exception {
        Files.deleteIfExists(DB_PENUH);
        Database.aturLokasi(DB_PENUH.toString());
        Database.inisialisasi();

        try (Connection koneksi = Database.koneksi(); Statement perintah = koneksi.createStatement()) {
            perintah.executeUpdate("DELETE FROM slot WHERE jenis = 'MOBIL'");
        }

        cekGagal("slot mobil habis ditolak", SlotPenuhException.class,
                () -> parkir.masuk(JenisKendaraan.MOBIL, null, admin));
        cek("slot motor tetap bisa", parkir.masuk(JenisKendaraan.MOTOR, null, admin) != null);
    }

    private static void ujiPersistensi() throws Exception {
        Database.aturLokasi(DB_UJI.toString());
        try (Connection koneksi = Database.koneksi(); Statement perintah = koneksi.createStatement();
                var rs = perintah.executeQuery("SELECT COUNT(*) FROM tiket")) {
            cek("tiket tersimpan di basis data", rs.next() && rs.getInt(1) >= 8);
        }
        cek("tarif terbaca ulang dari basis data", tarifService.semua().size() == 2);
    }

    // --------------------------------------------------------------- bantu

    private static boolean slotKosong(String nomor) {
        return slotService.semua().stream()
                .anyMatch(s -> s.getNomor().equals(nomor) && s.isKosong());
    }

    private static boolean slotTerisi(String nomor) {
        return slotService.semua().stream()
                .anyMatch(s -> s.getNomor().equals(nomor) && !s.isKosong());
    }

    private static void cek(String nama, boolean kondisi) {
        if (kondisi) {
            lulus++;
            System.out.println("[OK]     " + nama);
        } else {
            gagal++;
            System.out.println("[GAGAL]  " + nama);
        }
    }

    private static void cekGagal(String nama, Class<? extends Exception> diharapkan, Tindakan tindakan) {
        try {
            tindakan.jalankan();
            gagal++;
            System.out.println("[GAGAL]  " + nama + " (tidak melempar " + diharapkan.getSimpleName() + ")");
        } catch (Exception e) {
            if (diharapkan.isInstance(e)) {
                lulus++;
                System.out.println("[OK]     " + nama);
            } else {
                gagal++;
                System.out.println("[GAGAL]  " + nama + " (melempar " + e.getClass().getSimpleName()
                        + ": " + e.getMessage() + ")");
            }
        }
    }

    @FunctionalInterface
    private interface Tindakan {
        void jalankan() throws Exception;
    }
}
