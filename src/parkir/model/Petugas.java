package parkir.model;

import parkir.exception.ValidasiException;
import parkir.util.Hash;

public class Petugas {

    public static final String ROLE = "PETUGAS";

    private final int id;
    private String username;
    private String nama;
    private String passwordHash;
    private String salt;

    public Petugas(int id, String username, String nama, String passwordHash, String salt) throws ValidasiException {
        this.id = id;
        setUsername(username);
        setNama(nama);
        if (passwordHash == null || passwordHash.isBlank() || salt == null || salt.isBlank()) {
            throw new ValidasiException("Password petugas belum diset.");
        }
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    /** Membuat petugas baru (belum punya id) dengan password yang di-hash. */
    public static Petugas baru(String username, String nama, String password) throws ValidasiException {
        if (password == null || password.length() < 6) {
            throw new ValidasiException("Password minimal 6 karakter.");
        }
        String salt = Hash.buatSalt();
        return new Petugas(0, username, nama, Hash.hash(password, salt), salt);
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public final void setUsername(String username) throws ValidasiException {
        if (username == null || username.isBlank()) {
            throw new ValidasiException("Username wajib diisi.");
        }
        if (!username.matches("[a-zA-Z0-9._]{3,32}")) {
            throw new ValidasiException("Username 3-32 karakter, hanya huruf, angka, titik, atau garis bawah.");
        }
        this.username = username.trim().toLowerCase();
    }

    public String getNama() {
        return nama;
    }

    public final void setNama(String nama) throws ValidasiException {
        if (nama == null || nama.isBlank()) {
            throw new ValidasiException("Nama petugas wajib diisi.");
        }
        this.nama = nama.trim();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    /** Mengganti password: hash dan salt dibuat ulang. */
    public void gantiPassword(String password) throws ValidasiException {
        if (password == null || password.length() < 6) {
            throw new ValidasiException("Password minimal 6 karakter.");
        }
        this.salt = Hash.buatSalt();
        this.passwordHash = Hash.hash(password, salt);
    }

    public boolean verifikasiPassword(String input) {
        return Hash.cocok(input, salt, passwordHash);
    }

    public String getRole() {
        return ROLE;
    }

    @Override
    public String toString() {
        return nama + " (" + username + ")";
    }
}
