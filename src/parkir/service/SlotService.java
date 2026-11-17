package parkir.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import parkir.dao.Database;
import parkir.dao.SlotDAO;
import parkir.exception.DataAccessException;
import parkir.exception.ValidasiException;
import parkir.model.JenisKendaraan;
import parkir.model.Petugas;
import parkir.model.SlotParkir;
import parkir.model.StatusSlot;

/** CRUD Slot Parkir (entitas pertama untuk ketentuan CRUD lengkap). */
public class SlotService {

    private final SlotDAO slotDAO = new SlotDAO();
    private final AuditService audit = new AuditService();

    public List<SlotParkir> semua() {
        try (Connection koneksi = Database.koneksi()) {
            return slotDAO.semua(koneksi);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    public List<SlotParkir> kosong(JenisKendaraan jenis) {
        try (Connection koneksi = Database.koneksi()) {
            return slotDAO.berdasarkanStatus(koneksi, jenis, StatusSlot.KOSONG);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    public Map<JenisKendaraan, int[]> rekapKapasitas() {
        try (Connection koneksi = Database.koneksi()) {
            return slotDAO.rekapKapasitas(koneksi);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    public SlotParkir tambah(String nomor, JenisKendaraan jenis, Petugas oleh) throws ValidasiException {
        try (Connection koneksi = Database.koneksi()) {
            if (slotDAO.cariNomor(koneksi, nomor) != null) {
                throw new ValidasiException("Nomor slot " + nomor + " sudah dipakai.");
            }
            SlotParkir slot = new SlotParkir(0, nomor, jenis, StatusSlot.KOSONG);
            int id = slotDAO.simpan(koneksi, slot);
            SlotParkir tersimpan = new SlotParkir(id, slot.getNomor(), jenis, StatusSlot.KOSONG);
            audit.catat(koneksi, oleh, "TAMBAH_SLOT", "slot", String.valueOf(id),
                    tersimpan.getNomor() + " " + jenis.getLabel());
            return tersimpan;
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    /**
     * Mengubah data slot. Jenis slot tidak boleh diubah bila slot sedang
     * ditempati kendaraan.
     */
    public void ubah(int id, String nomor, JenisKendaraan jenis, Petugas oleh) throws ValidasiException {
        try (Connection koneksi = Database.koneksi()) {
            SlotParkir lama = slotDAO.cariId(koneksi, id);
            if (lama == null) {
                throw new ValidasiException("Slot tidak ditemukan.");
            }
            if (lama.getStatus() == StatusSlot.TERISI && lama.getJenis() != jenis) {
                throw new ValidasiException("Jenis slot tidak boleh diubah saat slot sedang terisi.");
            }
            SlotParkir cek = slotDAO.cariNomor(koneksi, nomor);
            if (cek != null && cek.getId() != id) {
                throw new ValidasiException("Nomor slot " + nomor + " sudah dipakai.");
            }
            SlotParkir baru = new SlotParkir(id, nomor, jenis, lama.getStatus());
            slotDAO.ubah(koneksi, baru);
            audit.catat(koneksi, oleh, "UBAH_SLOT", "slot", String.valueOf(id),
                    lama.getNomor() + " -> " + baru.getNomor());
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    /** Menghapus slot. Slot yang sedang terisi ditolak. */
    public void hapus(int id, Petugas oleh) throws ValidasiException {
        try (Connection koneksi = Database.koneksi()) {
            SlotParkir lama = slotDAO.cariId(koneksi, id);
            if (lama == null) {
                throw new ValidasiException("Slot tidak ditemukan.");
            }
            if (lama.getStatus() == StatusSlot.TERISI) {
                throw new ValidasiException("Slot " + lama.getNomor()
                        + " masih terisi. Kosongkan dulu sebelum menghapus.");
            }
            slotDAO.hapus(koneksi, id);
            audit.catat(koneksi, oleh, "HAPUS_SLOT", "slot", String.valueOf(id), lama.getNomor());
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }
}
