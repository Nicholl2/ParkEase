package parkir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import parkir.exception.DataAccessException;

public class PembayaranDAO {

    public int simpan(Connection koneksi, int tiketId, String metode, long jumlah, String waktu, int petugasId) {
        String sql = "INSERT INTO pembayaran (tiket_id, metode, jumlah, waktu, petugas_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = koneksi.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tiketId);
            ps.setString(2, metode);
            ps.setLong(3, jumlah);
            ps.setString(4, waktu);
            ps.setInt(5, petugasId);
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                return kunci.next() ? kunci.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menyimpan pembayaran: " + e.getMessage(), e);
        }
    }

    public int hitungByPetugas(Connection koneksi, int petugasId) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "SELECT COUNT(*) FROM pembayaran WHERE petugas_id = ?")) {
            ps.setInt(1, petugasId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menghitung pembayaran petugas: " + e.getMessage(), e);
        }
    }
}
