package parkir.service;

import java.sql.Connection;
import java.sql.SQLException;

import parkir.dao.Database;
import parkir.dao.PetugasDAO;
import parkir.exception.AutentikasiGagalException;
import parkir.exception.DataAccessException;
import parkir.model.Petugas;

public class AuthService {

    private final PetugasDAO petugasDAO = new PetugasDAO();
    private final AuditService audit = new AuditService();

    /**
     * Memverifikasi kredensial. Pesan gagal tidak membedakan username salah
     * dengan password salah.
     */
    public Petugas login(String username, String password) throws AutentikasiGagalException {
        try (Connection koneksi = Database.koneksi()) {
            Petugas petugas = petugasDAO.cariUsername(koneksi, username);
            if (petugas == null || !petugas.verifikasiPassword(password)) {
                audit.catat(koneksi, null, "LOGIN_GAGAL", "petugas",
                        username == null ? "-" : username, "Percobaan login gagal");
                throw new AutentikasiGagalException();
            }
            audit.catat(koneksi, petugas, "LOGIN", "petugas", String.valueOf(petugas.getId()),
                    petugas.getNama() + " login");
            return petugas;
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menutup koneksi: " + e.getMessage(), e);
        }
    }
}
