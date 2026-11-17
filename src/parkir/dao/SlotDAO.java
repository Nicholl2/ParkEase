package parkir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import parkir.exception.DataAccessException;
import parkir.exception.ValidasiException;
import parkir.model.JenisKendaraan;
import parkir.model.SlotParkir;
import parkir.model.StatusSlot;

public class SlotDAO {

    private SlotParkir petakan(ResultSet rs) throws SQLException, ValidasiException {
        return new SlotParkir(
                rs.getInt("id"),
                rs.getString("nomor"),
                JenisKendaraan.dari(rs.getString("jenis")),
                StatusSlot.valueOf(rs.getString("status")));
    }

    public List<SlotParkir> semua(Connection koneksi) {
        String sql = "SELECT * FROM slot ORDER BY jenis, nomor";
        List<SlotParkir> hasil = new ArrayList<>();
        try (PreparedStatement ps = koneksi.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(petakan(rs));
            }
            return hasil;
        } catch (SQLException | ValidasiException e) {
            throw new DataAccessException("Gagal membaca data slot: " + e.getMessage(), e);
        }
    }

    public List<SlotParkir> berdasarkanStatus(Connection koneksi, JenisKendaraan jenis, StatusSlot status) {
        String sql = "SELECT * FROM slot WHERE jenis = ? AND status = ? ORDER BY nomor";
        List<SlotParkir> hasil = new ArrayList<>();
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, jenis.name());
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(petakan(rs));
                }
            }
            return hasil;
        } catch (SQLException | ValidasiException e) {
            throw new DataAccessException("Gagal membaca slot: " + e.getMessage(), e);
        }
    }

    public SlotParkir cariNomor(Connection koneksi, String nomor) {
        String sql = "SELECT * FROM slot WHERE nomor = ?";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, nomor.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        } catch (SQLException | ValidasiException e) {
            throw new DataAccessException("Gagal mencari slot: " + e.getMessage(), e);
        }
    }

    public SlotParkir cariId(Connection koneksi, int id) {
        String sql = "SELECT * FROM slot WHERE id = ?";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        } catch (SQLException | ValidasiException e) {
            throw new DataAccessException("Gagal mencari slot: " + e.getMessage(), e);
        }
    }

    public int simpan(Connection koneksi, SlotParkir slot) {
        String sql = "INSERT INTO slot (nomor, jenis, status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = koneksi.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, slot.getNomor());
            ps.setString(2, slot.getJenis().name());
            ps.setString(3, slot.getStatus().name());
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                return kunci.next() ? kunci.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menambah slot: " + e.getMessage(), e);
        }
    }

    public void ubah(Connection koneksi, SlotParkir slot) {
        String sql = "UPDATE slot SET nomor = ?, jenis = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, slot.getNomor());
            ps.setString(2, slot.getJenis().name());
            ps.setString(3, slot.getStatus().name());
            ps.setInt(4, slot.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mengubah slot: " + e.getMessage(), e);
        }
    }

    public void hapus(Connection koneksi, int id) {
        try (PreparedStatement ps = koneksi.prepareStatement("DELETE FROM slot WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menghapus slot: " + e.getMessage(), e);
        }
    }

    public void setStatus(Connection koneksi, int id, StatusSlot status) {
        try (PreparedStatement ps = koneksi.prepareStatement("UPDATE slot SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Gagal memperbarui status slot: " + e.getMessage(), e);
        }
    }

    public int hitung(Connection koneksi, JenisKendaraan jenis, StatusSlot status) {
        String sql = "SELECT COUNT(*) FROM slot WHERE jenis = ? AND status = ?";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, jenis.name());
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menghitung slot: " + e.getMessage(), e);
        }
    }

    /** Rekap kapasitas per jenis: int[]{terisi, total}. */
    public Map<JenisKendaraan, int[]> rekapKapasitas(Connection koneksi) {
        String sql = "SELECT jenis, COUNT(*) AS total, "
                + "SUM(CASE WHEN status = 'TERISI' THEN 1 ELSE 0 END) AS terisi "
                + "FROM slot GROUP BY jenis";
        Map<JenisKendaraan, int[]> hasil = new EnumMap<>(JenisKendaraan.class);
        try (PreparedStatement ps = koneksi.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.put(JenisKendaraan.dari(rs.getString("jenis")),
                        new int[] { rs.getInt("terisi"), rs.getInt("total") });
            }
            return hasil;
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menghitung kapasitas: " + e.getMessage(), e);
        }
    }
}
