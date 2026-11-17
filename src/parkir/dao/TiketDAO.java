package parkir.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import parkir.exception.DataAccessException;
import parkir.exception.ValidasiException;
import parkir.model.JenisKendaraan;
import parkir.model.PengaturanTarif;
import parkir.model.SlotParkir;
import parkir.model.StatusSlot;
import parkir.model.StatusTiket;
import parkir.model.Tiket;

public class TiketDAO {

    private static final String SELECT_DASAR = "SELECT t.*, s.nomor AS slot_nomor, s.jenis AS slot_jenis, "
            + "s.status AS slot_status, pm.nama AS nama_masuk, ps.nama AS nama_selesai "
            + "FROM tiket t "
            + "JOIN slot s ON s.id = t.slot_id "
            + "JOIN petugas pm ON pm.id = t.petugas_masuk_id "
            + "LEFT JOIN petugas ps ON ps.id = t.petugas_selesai_id ";

    private Tiket petakan(ResultSet rs) throws SQLException, ValidasiException {
        JenisKendaraan jenis = JenisKendaraan.dari(rs.getString("jenis_kendaraan"));
        SlotParkir slot = new SlotParkir(
                rs.getInt("slot_id"),
                rs.getString("slot_nomor"),
                JenisKendaraan.dari(rs.getString("slot_jenis")),
                StatusSlot.valueOf(rs.getString("slot_status")));

        PengaturanTarif snapshot = new PengaturanTarif(
                jenis,
                rs.getLong("tarif_jam_pertama_snapshot"),
                rs.getLong("tarif_jam_berikutnya_snapshot"),
                rs.getInt("batas_jam_reguler_snapshot"),
                rs.getLong("denda_overstay_snapshot"),
                rs.getLong("denda_tiket_hilang_snapshot"));

        Tiket tiket = new Tiket(
                rs.getString("nomor_tiket"),
                slot,
                jenis,
                LocalDateTime.parse(rs.getString("waktu_masuk")),
                snapshot,
                rs.getInt("petugas_masuk_id"),
                rs.getString("nama_masuk"));

        tiket.setId(rs.getInt("id"));
        String keluar = rs.getString("waktu_keluar");
        if (keluar != null) {
            tiket.setWaktuKeluar(LocalDateTime.parse(keluar));
        }
        tiket.setDurasiJam(rs.getInt("durasi_jam"));
        tiket.setDendaOverstay(rs.getLong("denda_overstay"));
        tiket.setDendaTiketHilang(rs.getLong("denda_tiket_hilang"));
        tiket.setTotal(rs.getLong("total"));
        tiket.setJalurTiketHilang(rs.getInt("jalur_tiket_hilang") == 1);
        tiket.setStatus(StatusTiket.valueOf(rs.getString("status")));
        tiket.setAlasanPembatalan(rs.getString("alasan_pembatalan"));
        if (rs.getObject("petugas_selesai_id") != null) {
            tiket.setPetugasSelesai(rs.getInt("petugas_selesai_id"), rs.getString("nama_selesai"));
        }
        return tiket;
    }

    public int simpan(Connection koneksi, Tiket tiket) {
        String sql = "INSERT INTO tiket (nomor_tiket, slot_id, jenis_kendaraan, waktu_masuk, "
                + "tarif_jam_pertama_snapshot, tarif_jam_berikutnya_snapshot, batas_jam_reguler_snapshot, "
                + "denda_overstay_snapshot, denda_tiket_hilang_snapshot, jalur_tiket_hilang, "
                + "overstay, status, petugas_masuk_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'AKTIF', ?)";
        try (PreparedStatement ps = koneksi.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tiket.getNomorTiket());
            ps.setInt(2, tiket.getSlot().getId());
            ps.setString(3, tiket.getJenis().name());
            ps.setString(4, tiket.getWaktuMasuk().toString());
            ps.setLong(5, tiket.getTarifJamPertamaSnapshot());
            ps.setLong(6, tiket.getTarifJamBerikutnyaSnapshot());
            ps.setInt(7, tiket.getBatasJamRegulerSnapshot());
            ps.setLong(8, tiket.getDendaOverstaySnapshot());
            ps.setLong(9, tiket.getDendaTiketHilangSnapshot());
            ps.setInt(10, tiket.isJalurTiketHilang() ? 1 : 0);
            ps.setInt(11, tiket.getPetugasMasukId());
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                return kunci.next() ? kunci.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menyimpan tiket: " + e.getMessage(), e);
        }
    }

    public void perbarui(Connection koneksi, Tiket tiket) {
        String sql = "UPDATE tiket SET waktu_keluar = ?, durasi_jam = ?, overstay = ?, jalur_tiket_hilang = ?, "
                + "denda_overstay = ?, denda_tiket_hilang = ?, total = ?, status = ?, alasan_pembatalan = ?, "
                + "petugas_selesai_id = ? WHERE id = ?";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, tiket.getWaktuKeluar() == null ? null : tiket.getWaktuKeluar().toString());
            if (tiket.getDurasiJam() > 0) {
                ps.setInt(2, tiket.getDurasiJam());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            boolean overstay = tiket.getDurasiJam() > tiket.getBatasJamRegulerSnapshot();
            ps.setInt(3, overstay ? 1 : 0);
            ps.setInt(4, tiket.isJalurTiketHilang() ? 1 : 0);
            ps.setLong(5, tiket.getDendaOverstay());
            ps.setLong(6, tiket.getDendaTiketHilang());
            ps.setLong(7, tiket.getTotal());
            ps.setString(8, tiket.getStatus().name());
            ps.setString(9, tiket.getAlasanPembatalan());
            if (tiket.getPetugasSelesaiId() > 0) {
                ps.setInt(10, tiket.getPetugasSelesaiId());
            } else {
                ps.setNull(10, Types.INTEGER);
            }
            ps.setInt(11, tiket.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Gagal memperbarui tiket: " + e.getMessage(), e);
        }
    }

    public Tiket cariAktifByNomor(Connection koneksi, String nomor) {
        String sql = SELECT_DASAR + "WHERE t.nomor_tiket = ? AND t.status = 'AKTIF'";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, nomor == null ? "" : nomor.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        } catch (SQLException | ValidasiException e) {
            throw new DataAccessException("Gagal mencari tiket: " + e.getMessage(), e);
        }
    }

    public Tiket cariAktifBySlot(Connection koneksi, int slotId) {
        String sql = SELECT_DASAR + "WHERE t.slot_id = ? AND t.status = 'AKTIF'";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        } catch (SQLException | ValidasiException e) {
            throw new DataAccessException("Gagal mencari tiket: " + e.getMessage(), e);
        }
    }

    public List<Tiket> daftarAktif(Connection koneksi) {        String sql = SELECT_DASAR + "WHERE t.status = 'AKTIF' ORDER BY t.waktu_masuk";
        return bacaDaftar(koneksi, sql, ps -> {
        });
    }

    /**
     * Pencarian riwayat: kata kunci nomor tiket, status, dan jenis kendaraan.
     * Semua nilai dimasukkan lewat parameter, bukan disambung ke SQL.
     */
    public List<Tiket> cariRiwayat(Connection koneksi, String kataKunci, StatusTiket status, JenisKendaraan jenis) {
        StringBuilder sql = new StringBuilder(SELECT_DASAR).append("WHERE 1 = 1 ");
        boolean adaKunci = kataKunci != null && !kataKunci.isBlank();
        if (adaKunci) {
            sql.append("AND t.nomor_tiket LIKE ? ");
        }
        if (status != null) {
            sql.append("AND t.status = ? ");
        }
        if (jenis != null) {
            sql.append("AND t.jenis_kendaraan = ? ");
        }
        sql.append("ORDER BY t.waktu_masuk DESC LIMIT 500");

        return bacaDaftar(koneksi, sql.toString(), ps -> {
            try {
                int i = 1;
                if (adaKunci) {
                    ps.setString(i++, "%" + kataKunci.trim().toUpperCase() + "%");
                }
                if (status != null) {
                    ps.setString(i++, status.name());
                }
                if (jenis != null) {
                    ps.setString(i++, jenis.name());
                }
            } catch (SQLException e) {
                throw new DataAccessException("Gagal menyiapkan pencarian: " + e.getMessage(), e);
            }
        });
    }

    /** Jumlah tiket yang pernah ditangani petugas (sebagai pencatat masuk atau penyelesai). */
    public int hitungByPetugas(Connection koneksi, int petugasId) {
        String sql = "SELECT COUNT(*) FROM tiket WHERE petugas_masuk_id = ? OR petugas_selesai_id = ?";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setInt(1, petugasId);
            ps.setInt(2, petugasId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menghitung tiket petugas: " + e.getMessage(), e);
        }
    }

    private interface Pengisi {
        void isi(PreparedStatement ps);
    }

    private List<Tiket> bacaDaftar(Connection koneksi, String sql, Pengisi pengisi) {
        List<Tiket> hasil = new ArrayList<>();
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            pengisi.isi(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(petakan(rs));
                }
            }
            return hasil;
        } catch (SQLException | ValidasiException e) {
            throw new DataAccessException("Gagal membaca tiket: " + e.getMessage(), e);
        }
    }
}
