package parkir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import parkir.exception.DataAccessException;
import parkir.exception.ValidasiException;
import parkir.model.Petugas;
import parkir.util.Jam;

public class PetugasDAO {

    private Petugas petakan(ResultSet rs) throws SQLException, ValidasiException {
        return new Petugas(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("nama"),
                rs.getString("password_hash"),
                rs.getString("salt"));
    }

    public List<Petugas> semua(Connection koneksi) {
        List<Petugas> hasil = new ArrayList<>();
        try (PreparedStatement ps = koneksi.prepareStatement("SELECT * FROM petugas ORDER BY nama");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(petakan(rs));
            }
            return hasil;
        } catch (SQLException | ValidasiException e) {
            throw new DataAccessException("Gagal membaca data petugas: " + e.getMessage(), e);
        }
    }

    public Petugas cariUsername(Connection koneksi, String username) {
        try (PreparedStatement ps = koneksi.prepareStatement("SELECT * FROM petugas WHERE username = ?")) {
            ps.setString(1, username == null ? "" : username.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        } catch (SQLException | ValidasiException e) {
            throw new DataAccessException("Gagal mencari petugas: " + e.getMessage(), e);
        }
    }

    public Petugas cariId(Connection koneksi, int id) {
        try (PreparedStatement ps = koneksi.prepareStatement("SELECT * FROM petugas WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        } catch (SQLException | ValidasiException e) {
            throw new DataAccessException("Gagal mencari petugas: " + e.getMessage(), e);
        }
    }

    public int simpan(Connection koneksi, Petugas petugas) {
        String sql = "INSERT INTO petugas (username, nama, password_hash, salt, role, dibuat_pada) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = koneksi.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, petugas.getUsername());
            ps.setString(2, petugas.getNama());
            ps.setString(3, petugas.getPasswordHash());
            ps.setString(4, petugas.getSalt());
            ps.setString(5, petugas.getRole());
            ps.setString(6, Jam.sekarang().toString());
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                return kunci.next() ? kunci.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menambah petugas: " + e.getMessage(), e);
        }
    }

    public void ubah(Connection koneksi, Petugas petugas) {
        String sql = "UPDATE petugas SET username = ?, nama = ?, password_hash = ?, salt = ? WHERE id = ?";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, petugas.getUsername());
            ps.setString(2, petugas.getNama());
            ps.setString(3, petugas.getPasswordHash());
            ps.setString(4, petugas.getSalt());
            ps.setInt(5, petugas.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mengubah petugas: " + e.getMessage(), e);
        }
    }

    public void hapus(Connection koneksi, int id) {
        try (PreparedStatement ps = koneksi.prepareStatement("DELETE FROM petugas WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menghapus petugas: " + e.getMessage(), e);
        }
    }
}
