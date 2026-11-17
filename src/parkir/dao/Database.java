package parkir.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import parkir.exception.DataAccessException;
import parkir.model.JenisKendaraan;
import parkir.model.PengaturanTarif;
import parkir.util.Hash;
import parkir.util.Jam;

/**
 * Koneksi SQLite, pembuatan skema, dan data awal.
 *
 * <p>Semua DAO menerima {@link Connection} dari luar supaya satu operasi bisnis
 * dapat berjalan dalam satu transaksi.</p>
 */
public final class Database {

    public static final String LOKASI_BAWAAN = "data/parkir.db";

    private static String lokasi = LOKASI_BAWAAN;

    private Database() {
    }

    public static synchronized void aturLokasi(String path) {
        lokasi = path;
    }

    public static synchronized String getLokasi() {
        return lokasi;
    }

    public static Connection koneksi() {
        try {
            File berkas = new File(lokasi);
            File induk = berkas.getAbsoluteFile().getParentFile();
            if (induk != null && !induk.exists()) {
                induk.mkdirs();
            }
            Connection koneksi = DriverManager.getConnection("jdbc:sqlite:" + lokasi);
            try (Statement perintah = koneksi.createStatement()) {
                perintah.execute("PRAGMA foreign_keys = ON");
            }
            return koneksi;
        } catch (SQLException e) {
            throw new DataAccessException("Gagal membuka basis data: " + e.getMessage(), e);
        }
    }

    /** Membuat tabel bila belum ada, lalu mengisi data awal bila masih kosong. */
    public static synchronized void inisialisasi() {
        try (Connection koneksi = koneksi(); Statement perintah = koneksi.createStatement()) {
            for (String ddl : SKEMA) {
                perintah.execute(ddl);
            }
            seed(koneksi);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menyiapkan basis data: " + e.getMessage(), e);
        }
    }

    private static void seed(Connection koneksi) throws SQLException {
        if (hitung(koneksi, "petugas") == 0) {
            String salt = Hash.buatSalt();
            String hash = Hash.hash("admin123", salt);
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "INSERT INTO petugas (username, nama, password_hash, salt, role, dibuat_pada) "
                            + "VALUES (?, ?, ?, ?, 'PETUGAS', ?)")) {
                ps.setString(1, "admin");
                ps.setString(2, "Administrator");
                ps.setString(3, hash);
                ps.setString(4, salt);
                ps.setString(5, Jam.sekarang().toString());
                ps.executeUpdate();
            }
        }

        if (hitung(koneksi, "slot") == 0) {
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "INSERT INTO slot (nomor, jenis, status) VALUES (?, ?, 'KOSONG')")) {
                for (int i = 1; i <= 120; i++) {
                    ps.setString(1, String.format("M-%03d", i));
                    ps.setString(2, JenisKendaraan.MOTOR.name());
                    ps.addBatch();
                }
                for (int i = 1; i <= 30; i++) {
                    ps.setString(1, String.format("C-%03d", i));
                    ps.setString(2, JenisKendaraan.MOBIL.name());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

        if (hitung(koneksi, "pengaturan_tarif") == 0) {
            for (JenisKendaraan jenis : JenisKendaraan.values()) {
                try {
                    PengaturanTarif tarif = PengaturanTarif.bawaan(jenis);
                    new PengaturanTarifDAO().simpan(koneksi, tarif);
                } catch (Exception e) {
                    throw new SQLException("Gagal mengisi tarif awal: " + e.getMessage(), e);
                }
            }
        }
    }

    private static int hitung(Connection koneksi, String tabel) throws SQLException {
        try (Statement perintah = koneksi.createStatement();
                ResultSet rs = perintah.executeQuery("SELECT COUNT(*) FROM " + tabel)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static final String[] SKEMA = {
            "CREATE TABLE IF NOT EXISTS petugas ("
                    + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " username TEXT NOT NULL UNIQUE,"
                    + " nama TEXT NOT NULL,"
                    + " password_hash TEXT NOT NULL,"
                    + " salt TEXT NOT NULL,"
                    + " role TEXT NOT NULL DEFAULT 'PETUGAS',"
                    + " dibuat_pada TEXT NOT NULL)",

            "CREATE TABLE IF NOT EXISTS slot ("
                    + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " nomor TEXT NOT NULL UNIQUE,"
                    + " jenis TEXT NOT NULL CHECK (jenis IN ('MOTOR','MOBIL')),"
                    + " status TEXT NOT NULL DEFAULT 'KOSONG' CHECK (status IN ('KOSONG','TERISI')))",

            "CREATE TABLE IF NOT EXISTS pengaturan_tarif ("
                    + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " jenis TEXT NOT NULL UNIQUE CHECK (jenis IN ('MOTOR','MOBIL')),"
                    + " tarif_jam_pertama INTEGER NOT NULL CHECK (tarif_jam_pertama >= 0),"
                    + " tarif_jam_berikutnya INTEGER NOT NULL CHECK (tarif_jam_berikutnya >= 0),"
                    + " batas_jam_reguler INTEGER NOT NULL DEFAULT 8 CHECK (batas_jam_reguler > 0),"
                    + " denda_overstay INTEGER NOT NULL CHECK (denda_overstay >= 0),"
                    + " denda_tiket_hilang INTEGER NOT NULL CHECK (denda_tiket_hilang >= 0),"
                    + " berlaku_sejak TEXT NOT NULL)",

            "CREATE TABLE IF NOT EXISTS tiket ("
                    + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " nomor_tiket TEXT NOT NULL UNIQUE,"
                    + " slot_id INTEGER NOT NULL REFERENCES slot(id),"
                    + " jenis_kendaraan TEXT NOT NULL CHECK (jenis_kendaraan IN ('MOTOR','MOBIL')),"
                    + " waktu_masuk TEXT NOT NULL,"
                    + " waktu_keluar TEXT,"
                    + " durasi_jam INTEGER CHECK (durasi_jam IS NULL OR durasi_jam > 0),"
                    + " tarif_jam_pertama_snapshot INTEGER NOT NULL CHECK (tarif_jam_pertama_snapshot >= 0),"
                    + " tarif_jam_berikutnya_snapshot INTEGER NOT NULL CHECK (tarif_jam_berikutnya_snapshot >= 0),"
                    + " batas_jam_reguler_snapshot INTEGER NOT NULL CHECK (batas_jam_reguler_snapshot > 0),"
                    + " denda_overstay_snapshot INTEGER NOT NULL DEFAULT 0 CHECK (denda_overstay_snapshot >= 0),"
                    + " denda_tiket_hilang_snapshot INTEGER NOT NULL DEFAULT 0 CHECK (denda_tiket_hilang_snapshot >= 0),"
                    + " overstay INTEGER NOT NULL DEFAULT 0 CHECK (overstay IN (0,1)),"
                    + " jalur_tiket_hilang INTEGER NOT NULL DEFAULT 0 CHECK (jalur_tiket_hilang IN (0,1)),"
                    + " denda_overstay INTEGER NOT NULL DEFAULT 0 CHECK (denda_overstay >= 0),"
                    + " denda_tiket_hilang INTEGER NOT NULL DEFAULT 0 CHECK (denda_tiket_hilang >= 0),"
                    + " total INTEGER CHECK (total IS NULL OR total >= 0),"
                    + " status TEXT NOT NULL DEFAULT 'AKTIF' CHECK (status IN ('AKTIF','SELESAI','DIBATALKAN')),"
                    + " alasan_pembatalan TEXT,"
                    + " petugas_masuk_id INTEGER NOT NULL REFERENCES petugas(id),"
                    + " petugas_selesai_id INTEGER REFERENCES petugas(id),"
                    + " CHECK (status = 'AKTIF' OR waktu_keluar IS NOT NULL),"
                    + " CHECK (status <> 'DIBATALKAN' OR alasan_pembatalan IS NOT NULL))",

            "CREATE UNIQUE INDEX IF NOT EXISTS idx_tiket_slot_aktif ON tiket(slot_id) WHERE status = 'AKTIF'",
            "CREATE INDEX IF NOT EXISTS idx_tiket_nomor ON tiket(nomor_tiket)",
            "CREATE INDEX IF NOT EXISTS idx_tiket_masuk ON tiket(waktu_masuk)",

            "CREATE TABLE IF NOT EXISTS pembayaran ("
                    + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " tiket_id INTEGER NOT NULL REFERENCES tiket(id),"
                    + " metode TEXT NOT NULL CHECK (metode IN ('TUNAI','QRIS')),"
                    + " jumlah INTEGER NOT NULL CHECK (jumlah >= 0),"
                    + " waktu TEXT NOT NULL,"
                    + " petugas_id INTEGER NOT NULL REFERENCES petugas(id))",

            "CREATE TABLE IF NOT EXISTS audit_log ("
                    + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " waktu TEXT NOT NULL,"
                    + " petugas_id INTEGER REFERENCES petugas(id) ON DELETE SET NULL,"
                    + " petugas_nama TEXT,"
                    + " aksi TEXT NOT NULL,"
                    + " entitas TEXT NOT NULL,"
                    + " entitas_id TEXT,"
                    + " detail TEXT)",

            "CREATE INDEX IF NOT EXISTS idx_audit_waktu ON audit_log(waktu)" };

    /** Dipakai oleh skrip uji: mengosongkan data tanpa menghapus skema. */
    public static void kosongkanData() {
        try (Connection koneksi = koneksi(); Statement perintah = koneksi.createStatement()) {
            perintah.executeUpdate("DELETE FROM audit_log");
            perintah.executeUpdate("DELETE FROM pembayaran");
            perintah.executeUpdate("DELETE FROM tiket");
            perintah.executeUpdate("DELETE FROM slot");
            perintah.executeUpdate("DELETE FROM pengaturan_tarif");
            perintah.executeUpdate("DELETE FROM petugas");
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mengosongkan data: " + e.getMessage(), e);
        }
    }
}
