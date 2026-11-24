package parkir.ui;

import java.awt.BorderLayout;
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
        setMinimumSize(new Dimension(900, 580));
        setSize(1000, 660);
        setLocationRelativeTo(null);
        setBackground(Tema.BG);

        txtUsername.setPreferredSize(new Dimension(320, 40));
        txtPassword.setPreferredSize(new Dimension(320, 40));
        btnMasuk.setPreferredSize(new Dimension(320, 42));

        setContentPane(Tema.latar(new GridBagLayout()));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.weightx = 0.54;
        gbc.insets = new Insets(40, 56, 40, 20);
        add(buatPanelMerek(), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.46;
        gbc.insets = new Insets(40, 20, 40, 56);
        add(buatPanelFormulir(), gbc);

        pasangAksi();
        getRootPane().setDefaultButton(btnMasuk);
        txtUsername.requestFocusInWindow();
    }

    // ------------------------------------------------------------- tampilan

    private JPanel buatPanelMerek() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JPanel isi = new JPanel();
        isi.setOpaque(false);
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));
        isi.setAlignmentX(LEFT_ALIGNMENT);

        JPanel merek = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        merek.setOpaque(false);
        merek.setAlignmentX(LEFT_ALIGNMENT);
        merek.add(new Tema.IkonMerek(52));

        JLabel nama = new JLabel("Eparking");
        nama.setFont(Tema.FONT_JUDUL.deriveFont(34f));
        nama.setForeground(Tema.TEXT);
        nama.setBorder(new EmptyBorder(0, 14, 0, 0));
        nama.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        merek.add(nama);

        isi.add(merek);
        isi.add(Box.createVerticalStrut(10));

        JLabel tagline = new JLabel("Manajemen parkir yang rapi dan cepat");
        tagline.setFont(Tema.FONT_BADAN.deriveFont(14f));
        tagline.setForeground(Tema.TEXT_SEKUNDER);
        tagline.setAlignmentX(LEFT_ALIGNMENT);
        isi.add(tagline);

        isi.add(Box.createVerticalStrut(34));
        isi.add(poin("Tiket dan tarif dihitung otomatis"));
        isi.add(poin("Riwayat transaksi lengkap"));
        isi.add(poin("Denda overstay dan tiket hilang"));
        isi.add(poin("Data tersimpan di SQLite"));

        isi.add(Box.createVerticalStrut(30));

        JPanel catatan = Tema.panelLembut();
        catatan.setLayout(new BorderLayout(0, 4));
        catatan.setAlignmentX(LEFT_ALIGNMENT);
        catatan.setMaximumSize(new Dimension(360, 92));

        JLabel labelKecil = Tema.labelKapital("Area petugas");
        javax.swing.JTextArea isiKecil = Tema.catatan("Gunakan akun yang diberikan admin "
                + "untuk mencatat transaksi parkir.", 300);
        isiKecil.setFont(Tema.FONT_KECIL);
        isiKecil.setForeground(Tema.TEXT_SEKUNDER);
        catatan.add(labelKecil, BorderLayout.NORTH);
        catatan.add(isiKecil, BorderLayout.CENTER);
        isi.add(catatan);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(isi, gbc);
        return panel;
    }

    private JLabel poin(String teks) {
        JLabel label = new JLabel("<html><span style='color:#1856FF'>\u25CF</span>&nbsp;&nbsp;"
                + teks + "</html>");
        label.setFont(Tema.FONT_BADAN);
        label.setForeground(Tema.TEXT_SEKUNDER);
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 2, 10, 0));
        return label;
    }

    private JPanel buatPanelFormulir() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JPanel kartu = Tema.kartuBesar();
        kartu.setLayout(new GridBagLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        JLabel judul = new JLabel("Masuk ke akun petugas");
        judul.setFont(Tema.FONT_JUDUL.deriveFont(20f));
        judul.setForeground(Tema.TEXT);
        judul.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        JLabel keterangan = new JLabel("Gunakan akun yang terdaftar untuk mengelola parkir.");
        keterangan.setFont(Tema.FONT_KECIL);
        keterangan.setForeground(Tema.MUTED);

        lblPesan.setFont(Tema.FONT_BADAN);
        lblPesan.setForeground(Tema.DANGER);
        lblPesan.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        JCheckBox lihat = new JCheckBox("Tampilkan password");
        Tema.gayaCek(lihat);
        lihat.setFont(Tema.FONT_KECIL);
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
        form.add(petunjukBantuan(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        kartu.add(form, gbc);

        GridBagConstraints letak = new GridBagConstraints();
        letak.gridx = 0;
        letak.gridy = 0;
        letak.weightx = 1;
        letak.weighty = 1;
        letak.fill = GridBagConstraints.NONE;
        letak.anchor = GridBagConstraints.CENTER;
        panel.add(kartu, letak);
        return panel;
    }

    private JPanel petunjukBantuan() {
        JPanel panel = Tema.panelLembut();
        panel.setLayout(new BorderLayout(0, 3));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel judul = Tema.labelKapital("Butuh bantuan?");
        javax.swing.JTextArea info = Tema.catatan("Hubungi petugas admin untuk reset password.", 300);
        info.setFont(Tema.FONT_KECIL);
        info.setForeground(Tema.MUTED);
        panel.add(judul, BorderLayout.NORTH);
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
