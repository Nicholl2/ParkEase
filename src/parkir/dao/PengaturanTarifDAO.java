package parkir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import parkir.exception.DataAccessException;
import parkir.exception.ValidasiException;
import parkir.model.JenisKendaraan;
import parkir.model.PengaturanTarif;

public class PengaturanTarifDAO {

    private PengaturanTarif petakan(ResultSet rs) throws SQLException, ValidasiException {
        PengaturanTarif tarif = new PengaturanTarif(
                JenisKendaraan.dari(rs.getString("jenis")),
                rs.getLong("tarif_jam_pertama"),
                rs.getLong("tarif_jam_berikutnya"),
                rs.getInt("batas_jam_reguler"),
                rs.getLong("denda_overstay"),
                rs.getLong("denda_tiket_hilang"));
        tarif.setBerlakuSejak(LocalDateTime.parse(rs.getString("berlaku_sejak")));
        return tarif;
    }

    public List<PengaturanTarif> semua(Connection koneksi) {
        List<PengaturanTarif> hasil = new ArrayList<>();
        try (PreparedStatement ps = koneksi.prepareStatement("SELECT * FROM pengaturan_tarif ORDER BY jenis");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(petakan(rs));
            }
            return hasil;
        } catch (SQLException | ValidasiException e) {
            throw new DataAccessException("Gagal membaca tarif: " + e.getMessage(), e);
        }
    }

    public PengaturanTarif ambil(Connection koneksi, JenisKendaraan jenis) {
        try (PreparedStatement ps = koneksi.prepareStatement("SELECT * FROM pengaturan_tarif WHERE jenis = ?")) {
            ps.setString(1, jenis.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        } catch (SQLException | ValidasiException e) {
            throw new DataAccessException("Gagal membaca tarif: " + e.getMessage(), e);
        }
    }

    /** Upsert berdasarkan jenis kendaraan. */
    public void simpan(Connection koneksi, PengaturanTarif tarif) {
        String sql = "INSERT INTO pengaturan_tarif "
                + "(jenis, tarif_jam_pertama, tarif_jam_berikutnya, batas_jam_reguler, denda_overstay, "
                + "denda_tiket_hilang, berlaku_sejak) VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(jenis) DO UPDATE SET "
                + "tarif_jam_pertama = excluded.tarif_jam_pertama, "
                + "tarif_jam_berikutnya = excluded.tarif_jam_berikutnya, "
                + "batas_jam_reguler = excluded.batas_jam_reguler, "
                + "denda_overstay = excluded.denda_overstay, "
                + "denda_tiket_hilang = excluded.denda_tiket_hilang, "
                + "berlaku_sejak = excluded.berlaku_sejak";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, tarif.getJenis().name());
            ps.setLong(2, tarif.getTarifJamPertama());
            ps.setLong(3, tarif.getTarifJamBerikutnya());
            ps.setInt(4, tarif.getBatasJamReguler());
            ps.setLong(5, tarif.getDendaOverstay());
            ps.setLong(6, tarif.getDendaTiketHilang());
            ps.setString(7, tarif.getBerlakuSejak().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menyimpan tarif: " + e.getMessage(), e);
        }
    }
}
