package parkir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import parkir.exception.DataAccessException;
import parkir.model.AuditLog;

public class AuditLogDAO {

    /**
     * Menyimpan satu baris audit. Nama petugas ikut disimpan agar jejak tetap
     * terbaca meskipun akun petugasnya kelak dihapus.
     */
    public void simpan(Connection koneksi, LocalDateTime waktu, int petugasId, String petugasNama,
            String aksi, String entitas, String entitasId, String detail) {
        String sql = "INSERT INTO audit_log (waktu, petugas_id, petugas_nama, aksi, entitas, entitas_id, detail) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, waktu.toString());
            if (petugasId > 0) {
                ps.setInt(2, petugasId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, petugasNama);
            ps.setString(4, aksi);
            ps.setString(5, entitas);
            ps.setString(6, entitasId);
            ps.setString(7, detail);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menyimpan audit log: " + e.getMessage(), e);
        }
    }

    public List<AuditLog> terbaru(Connection koneksi, int batas) {
        String sql = "SELECT a.*, COALESCE(a.petugas_nama, p.nama) AS nama_petugas FROM audit_log a "
                + "LEFT JOIN petugas p ON p.id = a.petugas_id ORDER BY a.id DESC LIMIT ?";
        List<AuditLog> hasil = new ArrayList<>();
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setInt(1, batas);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new AuditLog(
                            rs.getInt("id"),
                            LocalDateTime.parse(rs.getString("waktu")),
                            rs.getInt("petugas_id"),
                            rs.getString("nama_petugas"),
                            rs.getString("aksi"),
                            rs.getString("entitas"),
                            rs.getString("entitas_id"),
                            rs.getString("detail")));
                }
            }
            return hasil;
        } catch (SQLException e) {
            throw new DataAccessException("Gagal membaca audit log: " + e.getMessage(), e);
        }
    }
}
