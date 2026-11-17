package parkir.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import parkir.dao.Database;
import parkir.dao.PembayaranDAO;
import parkir.dao.PetugasDAO;
import parkir.dao.TiketDAO;
import parkir.exception.DataAccessException;
import parkir.exception.ValidasiException;
import parkir.model.Petugas;

/** CRUD Petugas (entitas kedua untuk ketentuan CRUD lengkap). */
public class PetugasService {

    private final PetugasDAO petugasDAO = new PetugasDAO();
    private final TiketDAO tiketDAO = new TiketDAO();
    private final PembayaranDAO pembayaranDAO = new PembayaranDAO();
    private final AuditService audit = new AuditService();

    public List<Petugas> semua() {
        try (Connection koneksi = Database.koneksi()) {
            return petugasDAO.semua(koneksi);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    public Petugas tambah(String username, String nama, String password, Petugas oleh) throws ValidasiException {
        try (Connection koneksi = Database.koneksi()) {
            if (petugasDAO.cariUsername(koneksi, username) != null) {
                throw new ValidasiException("Username " + username + " sudah dipakai.");
            }
            Petugas baru = Petugas.baru(username, nama, password);
            int id = petugasDAO.simpan(koneksi, baru);
            audit.catat(koneksi, oleh, "TAMBAH_PETUGAS", "petugas", String.valueOf(id),
                    baru.getNama() + " (" + baru.getUsername() + ")");
            return petugasDAO.cariId(koneksi, id);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    public void ubah(int id, String username, String nama, Petugas oleh) throws ValidasiException {
        try (Connection koneksi = Database.koneksi()) {
            Petugas lama = petugasDAO.cariId(koneksi, id);
            if (lama == null) {
                throw new ValidasiException("Petugas tidak ditemukan.");
            }
            Petugas lain = petugasDAO.cariUsername(koneksi, username);
            if (lain != null && lain.getId() != id) {
                throw new ValidasiException("Username " + username + " sudah dipakai.");
            }
            Petugas baru = new Petugas(id, username, nama, lama.getPasswordHash(), lama.getSalt());
            petugasDAO.ubah(koneksi, baru);
            audit.catat(koneksi, oleh, "UBAH_PETUGAS", "petugas", String.valueOf(id),
                    lama.getNama() + " -> " + baru.getNama());
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    public void resetPassword(int id, String password, Petugas oleh) throws ValidasiException {
        try (Connection koneksi = Database.koneksi()) {
            Petugas petugas = petugasDAO.cariId(koneksi, id);
            if (petugas == null) {
                throw new ValidasiException("Petugas tidak ditemukan.");
            }
            petugas.gantiPassword(password);
            petugasDAO.ubah(koneksi, petugas);
            audit.catat(koneksi, oleh, "RESET_PASSWORD", "petugas", String.valueOf(id), petugas.getUsername());
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    /** Petugas yang sedang login tidak boleh menghapus akunnya sendiri. */
    public void hapus(int id, Petugas oleh) throws ValidasiException {
        try (Connection koneksi = Database.koneksi()) {
            if (oleh != null && oleh.getId() == id) {
                throw new ValidasiException("Anda tidak dapat menghapus akun yang sedang digunakan.");
            }
            Petugas petugas = petugasDAO.cariId(koneksi, id);
            if (petugas == null) {
                throw new ValidasiException("Petugas tidak ditemukan.");
            }
            if (tiketDAO.hitungByPetugas(koneksi, id) > 0 || pembayaranDAO.hitungByPetugas(koneksi, id) > 0) {
                throw new ValidasiException("Petugas " + petugas.getNama()
                        + " sudah memiliki riwayat transaksi sehingga tidak dapat dihapus.");
            }
            petugasDAO.hapus(koneksi, id);
            audit.catat(koneksi, oleh, "HAPUS_PETUGAS", "petugas", String.valueOf(id), petugas.getUsername());
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }
}
