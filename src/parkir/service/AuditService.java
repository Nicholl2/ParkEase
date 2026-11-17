package parkir.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import parkir.dao.AuditLogDAO;
import parkir.dao.Database;
import parkir.exception.DataAccessException;
import parkir.model.AuditLog;
import parkir.model.Petugas;
import parkir.util.Jam;

/** Pencatatan jejak audit untuk aksi sensitif. */
public class AuditService {

    private final AuditLogDAO dao = new AuditLogDAO();

    /** Mencatat audit memakai koneksi milik pemanggil (agar ikut transaksi). */
    public void catat(Connection koneksi, Petugas petugas, String aksi, String entitas,
            String entitasId, String detail) {
        dao.simpan(koneksi, Jam.sekarang(), petugas == null ? 0 : petugas.getId(),
                petugas == null ? null : petugas.getNama(), aksi, entitas, entitasId, detail);
    }

    public List<AuditLog> terbaru(int batas) {
        try (Connection koneksi = Database.koneksi()) {
            return dao.terbaru(koneksi, batas);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }
}
