package parkir.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import parkir.exception.DataAccessException;
import parkir.exception.ValidasiException;
import parkir.model.Petugas;

/** CRUD Petugas: tabel di kiri, formulir di kanan. */
public class PetugasPanel extends JPanel implements MuatUlang {

    private static final long serialVersionUID = 1L;

    private final MainFrame frame;

    private final JTable tabel = Tema.tabel("Nama", "Username");
    private final JLabel lblInfo = Tema.kecil("");

    private final JTextField txtNama = Tema.field(14);
    private final JTextField txtUsername = Tema.field(14);
    private final JPasswordField txtPassword = Tema.sandi(14);
    private final JButton btnTambah = Tema.utama("Tambah Petugas");
    private final JButton btnSimpan = Tema.kedua("Simpan Perubahan");
    private final JButton btnReset = Tema.kedua("Reset Password");
    private final JButton btnHapus = Tema.bahaya("Hapus Petugas");

    private List<Petugas> data = new ArrayList<>();
    private int idTerpilih = 0;

    public PetugasPanel(MainFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout(18, 0));

        add(buatPanelTabel(), BorderLayout.CENTER);
        add(buatFormulir(), BorderLayout.EAST);
        pasangAksi();
    }

    // ------------------------------------------------------------- tampilan

    private JPanel buatPanelTabel() {
        JPanel kartu = Tema.kartu();
        kartu.setLayout(new BorderLayout(0, 10));

        JLabel judul = Tema.subjudul("Daftar Petugas");
        kartu.add(judul, BorderLayout.NORTH);

        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = Tema.gulir(tabel);
        kartu.add(scroll, BorderLayout.CENTER);
        kartu.add(lblInfo, BorderLayout.SOUTH);
        return kartu;
    }

    private JPanel buatFormulir() {
        JPanel kartu = Tema.kartu();
        kartu.setPreferredSize(new Dimension(340, 0));
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));

        JLabel judul = Tema.subjudul("Data Petugas");
        judul.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(judul);
        kartu.add(Box.createVerticalStrut(4));

        JLabel keterangan = Tema.kecil("Password minimal 6 karakter.");
        keterangan.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(keterangan);
        kartu.add(Box.createVerticalStrut(16));

        kartu.add(Tema.berlabel("NAMA LENGKAP", txtNama));
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(Tema.berlabel("USERNAME", txtUsername));
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(Tema.berlabel("PASSWORD", txtPassword));
        kartu.add(Box.createVerticalStrut(10));

        JLabel catatanSandi = Tema.kecil("Dipakai saat menambah atau reset password.");
        catatanSandi.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(catatanSandi);
        kartu.add(Box.createVerticalStrut(20));

        for (JButton tombol : new JButton[] { btnTambah, btnSimpan, btnReset, btnHapus }) {
            tombol.setAlignmentX(LEFT_ALIGNMENT);
            tombol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            kartu.add(tombol);
            kartu.add(Box.createVerticalStrut(8));
        }

        kartu.add(Box.createVerticalStrut(8));
        JTextArea catatan = Tema.catatan("Petugas yang sudah memiliki riwayat transaksi tidak dapat "
                + "dihapus, tetapi audit log tetap menyimpan namanya.", 280);
        kartu.add(catatan);
        kartu.add(Box.createVerticalGlue());
        return kartu;
    }

    // ---------------------------------------------------------------- aksi

    private void pasangAksi() {
        btnTambah.addActionListener(e -> tambah());
        btnSimpan.addActionListener(e -> ubah());
        btnReset.addActionListener(e -> resetPassword());
        btnHapus.addActionListener(e -> hapus());
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                pilihBaris();
            }
        });
    }

    @Override
    public void muatUlang() {
        try {
            data = frame.petugasService().semua();
        } catch (DataAccessException e) {
            data = new ArrayList<>();
            galat("Gagal memuat petugas: " + e.getMessage());
        }
        List<Object[]> baris = new ArrayList<>();
        for (Petugas petugas : data) {
            baris.add(new Object[] { petugas.getNama(), petugas.getUsername() });
        }
        Tema.isiTabel(tabel, baris);
        lblInfo.setText(data.size() + " petugas terdaftar. Akun yang sedang dipakai tidak dapat dihapus.");
        bersihkanForm();
    }

    private void pilihBaris() {
        int baris = Tema.barisTerpilih(tabel);
        if (baris < 0 || baris >= data.size()) {
            return;
        }
        Petugas petugas = data.get(baris);
        idTerpilih = petugas.getId();
        txtNama.setText(petugas.getNama());
        txtUsername.setText(petugas.getUsername());
        txtPassword.setText("");
    }

    private void tambah() {
        try {
            Petugas baru = frame.petugasService().tambah(txtUsername.getText(), txtNama.getText(),
                    new String(txtPassword.getPassword()), frame.getPetugas());
            info("Petugas " + baru.getNama() + " berhasil ditambahkan.");
            muatUlang();
        } catch (ValidasiException e) {
            galat(e.getMessage());
        } catch (DataAccessException e) {
            galat("Gagal menyimpan petugas: " + e.getMessage());
        }
    }

    private void ubah() {
        if (idTerpilih == 0) {
            galat("Pilih petugas pada tabel terlebih dahulu.");
            return;
        }
        try {
            frame.petugasService().ubah(idTerpilih, txtUsername.getText(), txtNama.getText(), frame.getPetugas());
            info("Data petugas berhasil diubah.");
            muatUlang();
        } catch (ValidasiException e) {
            galat(e.getMessage());
        } catch (DataAccessException e) {
            galat("Gagal mengubah petugas: " + e.getMessage());
        }
    }

    private void resetPassword() {
        if (idTerpilih == 0) {
            galat("Pilih petugas pada tabel terlebih dahulu.");
            return;
        }
        String password = new String(txtPassword.getPassword());
        if (password.isBlank()) {
            galat("Isi password baru terlebih dahulu.");
            return;
        }
        int pilih = JOptionPane.showConfirmDialog(this,
                "Reset password untuk " + txtUsername.getText().trim() + "?",
                "Konfirmasi Reset Password", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (pilih != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            frame.petugasService().resetPassword(idTerpilih, password, frame.getPetugas());
            info("Password berhasil direset.");
            muatUlang();
        } catch (ValidasiException e) {
            galat(e.getMessage());
        } catch (DataAccessException e) {
            galat("Gagal mereset password: " + e.getMessage());
        }
    }

    private void hapus() {
        if (idTerpilih == 0) {
            galat("Pilih petugas yang akan dihapus.");
            return;
        }
        int pilih = JOptionPane.showConfirmDialog(this,
                "Hapus petugas " + txtNama.getText().trim() + "?\nTindakan ini tidak dapat dibatalkan.",
                "Konfirmasi Hapus Petugas", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (pilih != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            frame.petugasService().hapus(idTerpilih, frame.getPetugas());
            info("Petugas berhasil dihapus.");
            muatUlang();
        } catch (ValidasiException e) {
            galat(e.getMessage());
        } catch (DataAccessException e) {
            galat("Gagal menghapus petugas: " + e.getMessage());
        }
    }

    private void bersihkanForm() {
        idTerpilih = 0;
        txtNama.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        tabel.clearSelection();
    }

    private void info(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Berhasil", JOptionPane.INFORMATION_MESSAGE);
    }

    private void galat(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Tidak dapat diproses", JOptionPane.WARNING_MESSAGE);
    }
}
