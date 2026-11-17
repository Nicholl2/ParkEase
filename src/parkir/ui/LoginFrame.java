package parkir.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import parkir.exception.AutentikasiGagalException;
import parkir.exception.DataAccessException;
import parkir.model.Petugas;
import parkir.service.AuthService;

/**
 * Layar login.
 *
 * <p>Panel kiri menampilkan identitas aplikasi, panel kanan berisi formulir.
 * Keduanya memakai layout manager sehingga tampilan tetap rapi saat jendela
 * diperbesar.</p>
 */
public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final AuthService auth = new AuthService();

    private final JTextField txtUsername = Tema.field(22);
    private final JPasswordField txtPassword = Tema.sandi(22);
    private final JLabel lblPesan = new JLabel(" ");
    private final JButton btnMasuk = Tema.utama("Masuk");

    public LoginFrame() {
        setTitle("Eparking — Masuk");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(880, 560));
        setSize(980, 640);
        setLocationRelativeTo(null);

        txtUsername.setPreferredSize(new Dimension(320, 40));
        txtPassword.setPreferredSize(new Dimension(320, 40));
        btnMasuk.setPreferredSize(new Dimension(320, 42));

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.weightx = 0.45;
        add(buatPanelMerek(), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.55;
        add(buatPanelFormulir(), gbc);

        pasangAksi();
        getRootPane().setDefaultButton(btnMasuk);
        txtUsername.requestFocusInWindow();
    }

    // ------------------------------------------------------------- tampilan

    private JPanel buatPanelMerek() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Tema.SIDEBAR);

        JPanel isi = new JPanel();
        isi.setOpaque(false);
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));

        JLabel nama = new JLabel("Eparking");
        nama.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 34));
        nama.setForeground(Color.WHITE);
        nama.setAlignmentX(LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Manajemen parkir yang rapi dan cepat");
        tagline.setFont(Tema.FONT_BADAN);
        tagline.setForeground(new Color(0xB9D2CB));
        tagline.setAlignmentX(LEFT_ALIGNMENT);

        isi.add(nama);
        isi.add(Box.createVerticalStrut(6));
        isi.add(tagline);
        isi.add(Box.createVerticalStrut(36));
        isi.add(poin("Tiket dan tarif dihitung otomatis"));
        isi.add(poin("Riwayat transaksi lengkap"));
        isi.add(poin("Denda overstay dan tiket hilang"));
        isi.add(poin("Data tersimpan di SQLite"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 52, 0, 40);
        panel.add(isi, gbc);
        return panel;
    }

    private JLabel poin(String teks) {
        JLabel label = new JLabel("•  " + teks);
        label.setFont(Tema.FONT_BADAN);
        label.setForeground(new Color(0xD7E5E0));
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, 10, 0));
        return label;
    }

    private JPanel buatPanelFormulir() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Tema.SURFACE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        JLabel judul = new JLabel("Masuk ke akun petugas");
        judul.setFont(Tema.FONT_JUDUL.deriveFont(20f));
        judul.setForeground(Tema.TEXT);
        judul.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        JLabel keterangan = new JLabel("Gunakan akun yang terdaftar untuk mengelola parkir.");
        keterangan.setFont(Tema.FONT_BADAN);
        keterangan.setForeground(Tema.MUTED);

        lblPesan.setFont(Tema.FONT_BADAN);
        lblPesan.setForeground(Tema.DANGER);
        lblPesan.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        JCheckBox lihat = new JCheckBox("Tampilkan password");
        lihat.setOpaque(false);
        lihat.setFont(Tema.FONT_KECIL);
        lihat.setForeground(Tema.MUTED);
        lihat.addActionListener(e -> txtPassword.setEchoChar(lihat.isSelected() ? (char) 0 : '\u2022'));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 0);

        gbc.gridy = 0;
        form.add(judul, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 24, 0);
        form.add(keterangan, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 12, 0);
        form.add(Tema.berlabel("USERNAME", txtUsername), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 6, 0);
        form.add(Tema.berlabel("PASSWORD", txtPassword), gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(lihat, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(6, 0, 0, 0);
        form.add(lblPesan, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(6, 0, 20, 0);
        form.add(btnMasuk, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(petunjukAkun(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 56, 0, 56);
        panel.add(form, gbc);
        return panel;
    }

    private JPanel petunjukAkun() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xF2F7F5));
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel info = new JLabel("<html><b>Akun demo</b><br>admin / admin123</html>");
        info.setFont(Tema.FONT_BADAN);
        info.setForeground(Tema.MUTED);
        panel.add(info, BorderLayout.CENTER);
        return panel;
    }

    // ---------------------------------------------------------------- aksi

    private void pasangAksi() {
        btnMasuk.addActionListener(e -> masuk());
        txtUsername.addActionListener(e -> masuk());
        txtPassword.addActionListener(e -> masuk());
    }

    private void masuk() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            tampilkanPesan("Username dan password wajib diisi.");
            return;
        }

        btnMasuk.setEnabled(false);
        btnMasuk.setText("Memproses...");
        try {
            Petugas petugas = auth.login(username, password);
            new MainFrame(petugas).setVisible(true);
            dispose();
        } catch (AutentikasiGagalException e) {
            tampilkanPesan(e.getMessage());
            txtPassword.setText("");
            txtPassword.requestFocusInWindow();
        } catch (DataAccessException e) {
            tampilkanPesan("Basis data bermasalah: " + e.getMessage());
        } finally {
            btnMasuk.setEnabled(true);
            btnMasuk.setText("Masuk");
        }
    }

    private void tampilkanPesan(String pesan) {
        lblPesan.setText("<html>" + pesan + "</html>");
    }
}
