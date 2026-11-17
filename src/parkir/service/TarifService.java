package parkir.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import parkir.dao.Database;
import parkir.dao.PengaturanTarifDAO;
import parkir.exception.DataAccessException;
import parkir.exception.ValidasiException;
import parkir.model.JenisKendaraan;
import parkir.model.PengaturanTarif;
import parkir.model.Petugas;
import parkir.util.Jam;
import parkir.util.Money;

public class TarifService {

    private final PengaturanTarifDAO tarifDAO = new PengaturanTarifDAO();
    private final AuditService audit = new AuditService();

    public List<PengaturanTarif> semua() {
        try (Connection koneksi = Database.koneksi()) {
            return tarifDAO.semua(koneksi);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    public PengaturanTarif ambil(JenisKendaraan jenis) {
        try (Connection koneksi = Database.koneksi()) {
            return tarifDAO.ambil(koneksi, jenis);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }

    /**
     * Menyimpan perubahan tarif beserta jejak audit. Perubahan hanya berlaku
     * untuk tiket berikutnya karena tiket aktif memakai snapshot.
     */
    public void simpan(PengaturanTarif baru, Petugas oleh) throws ValidasiException {
        if (oleh == null) {
            throw new ValidasiException("Petugas wajib diketahui untuk mencatat perubahan tarif.");
        }
        try (Connection koneksi = Database.koneksi()) {
            PengaturanTarif lama = tarifDAO.ambil(koneksi, baru.getJenis());
            List<String> perubahan = new ArrayList<>();
            if (lama != null) {
                if (lama.getTarifJamPertama() != baru.getTarifJamPertama()) {
                    perubahan.add("jam pertama " + Money.format(lama.getTarifJamPertama()) + " -> "
                            + Money.format(baru.getTarifJamPertama()));
                }
                if (lama.getTarifJamBerikutnya() != baru.getTarifJamBerikutnya()) {
                    perubahan.add("jam berikutnya " + Money.format(lama.getTarifJamBerikutnya()) + " -> "
                            + Money.format(baru.getTarifJamBerikutnya()));
                }
                if (lama.getDendaOverstay() != baru.getDendaOverstay()) {
                    perubahan.add("denda overstay " + Money.format(lama.getDendaOverstay()) + " -> "
                            + Money.format(baru.getDendaOverstay()));
                }
                if (lama.getDendaTiketHilang() != baru.getDendaTiketHilang()) {
                    perubahan.add("denda tiket hilang " + Money.format(lama.getDendaTiketHilang()) + " -> "
                            + Money.format(baru.getDendaTiketHilang()));
                }
                if (lama.getBatasJamReguler() != baru.getBatasJamReguler()) {
                    perubahan.add("batas jam " + lama.getBatasJamReguler() + " -> " + baru.getBatasJamReguler());
                }
            }
            if (perubahan.isEmpty()) {
                return;
            }
            baru.setBerlakuSejak(Jam.sekarang());
            tarifDAO.simpan(koneksi, baru);
            audit.catat(koneksi, oleh, "UBAH_TARIF", "pengaturan_tarif", baru.getJenis().name(),
                    String.join("; ", perubahan));
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }
}
