package parkir.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import parkir.model.Petugas;
import parkir.service.AuditService;
import parkir.service.ParkirService;
import parkir.service.PetugasService;
import parkir.service.SlotService;
import parkir.service.TarifService;
import parkir.util.Jam;

/**
 * Jendela utama setelah login.
 *
 * <p>Navigasi tersedia lewat menu bar dan sidebar; keduanya memanggil
 * {@link #tampilkan(String)} yang sama sehingga perilakunya konsisten. Isi
 * halaman ditampung {@link CardLayout} dan setiap halaman yang mengimplementasi
 * {@link MuatUlang} akan menyegarkan datanya saat dibuka.</p>
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final String DASHBOARD = "Dashboard";
    public static final String MASUK = "Kendaraan Masuk";
    public static final String KELUAR = "Kendaraan Keluar";
    public static final String RIWAYAT = "Riwayat";
    public static final String SLOT = "Slot Parkir";
    public static final String PETUGAS = "Petugas";
    public static final String TARIF = "Tarif & Denda";
    public static final String DEMO = "Mode Demo";

    private final Petugas petugas;
    private final ParkirService parkir = new ParkirService();
    private final SlotService slot = new SlotService();
    private final PetugasService petugasService = new PetugasService();
    private final TarifService tarif = new TarifService();
    private final AuditService audit = new AuditService();

    private final CardLayout kartu = new CardLayout();
    private final JPanel isi = new JPanel(kartu);
    private final Map<String, JComponent> halaman = new LinkedHashMap<>();
    private final Map<String, String> keterangan = new LinkedHashMap<>();
    private final Map<String, Tema.TombolNav> navTombol = new LinkedHashMap<>();
    private String kunciAktif = DASHBOARD;

    private final JLabel lblJudul = new JLabel();
    private final JLabel lblKeterangan = new JLabel();
    private final JLabel lblJam = new JLabel();
    private final Timer timerJam;

    public MainFrame(Petugas petugas) {
        this.petugas = petugas;

        setTitle("Eparking — Manajemen Parkir");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 680));
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setBackground(Tema.BG);

        isi.setOpaque(false);
        isi.setBackground(Tema.BG);

        daftarkanHalaman();
        setJMenuBar(buatMenu());

        JPanel akar = Tema.latar();
        akar.add(buatSidebar(), BorderLayout.WEST);
        akar.add(buatKepala(), BorderLayout.NORTH);
        akar.add(isi, BorderLayout.CENTER);
        akar.add(buatStatusBar(), BorderLayout.SOUTH);
        setContentPane(akar);

        timerJam = new Timer(30_000, e -> perbaruiJam());
        timerJam.start();
        perbaruiJam();

        tampilkan(DASHBOARD);
    }

    // --------------------------------------------------------------- akses

    public Petugas getPetugas() {
        return petugas;
    }

    public ParkirService parkir() {
        return parkir;
    }

    public SlotService slot() {
        return slot;
    }

    public PetugasService petugasService() {
        return petugasService;
    }

    public TarifService tarif() {
        return tarif;
    }

    public AuditService audit() {
        return audit;
    }

    // ------------------------------------------------------------- halaman

    private void daftarkanHalaman() {
        daftar(DASHBOARD, "Ringkasan operasional parkir hari ini.", new DashboardPanel(this));
        daftar(MASUK, "Catat kendaraan yang baru masuk dan dapatkan nomor tiket.", new MasukPanel(this));
        daftar(KELUAR, "Cari tiket aktif, hitung biaya, lalu proses pembayaran.", new KeluarPanel(this));
        daftar(RIWAYAT, "Telusuri transaksi selesai, dibatalkan, maupun yang masih aktif.", new RiwayatPanel(this));
        daftar(SLOT, "Kelola data slot parkir: tambah, ubah, dan hapus.", new SlotPanel(this));
        daftar(PETUGAS, "Kelola akun petugas dan reset password.", new PetugasPanel(this));
        daftar(TARIF, "Atur tarif dan denda per jenis kendaraan.", new TarifPanel(this));
        daftar(DEMO, "Majukan jam aplikasi untuk menguji overstay tanpa menunggu.", new ModeDemoPanel(this));
    }

    private void daftar(String kunci, String subjudul, JComponent panel) {
        halaman.put(kunci, panel);
        keterangan.put(kunci, subjudul);

        JPanel bungkus = new JPanel(new BorderLayout());
        bungkus.setOpaque(false);
        bungkus.setBorder(new EmptyBorder(20, 24, 20, 24));
        bungkus.add(panel, BorderLayout.CENTER);
        isi.add(bungkus, kunci);
    }

    /** Menampilkan halaman dan menyegarkan datanya. */
    public void tampilkan(String kunci) {
        if (!halaman.containsKey(kunci)) {
            return;
        }
        kunciAktif = kunci;
        kartu.show(isi, kunci);
        lblJudul.setText(kunci);
        lblKeterangan.setText(keterangan.get(kunci));

        for (Map.Entry<String, Tema.TombolNav> entri : navTombol.entrySet()) {
            entri.getValue().setAktif(entri.getKey().equals(kunci));
        }

        JComponent panel = halaman.get(kunci);
        if (panel instanceof MuatUlang) {
            ((MuatUlang) panel).muatUlang();
        }
    }

    // ------------------------------------------------------------- sidebar

    private JPanel buatSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(Tema.BG_GELAP);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(248, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, Tema.BORDER),
                new EmptyBorder(24, 16, 20, 16)));

        sidebar.add(buatMerek());
        sidebar.add(Box.createVerticalStrut(26));

        sidebar.add(labelGrup("TRANSAKSI"));
        sidebar.add(tombolNav(DASHBOARD, "Dashboard"));
        sidebar.add(tombolNav(MASUK, "Kendaraan Masuk"));
        sidebar.add(tombolNav(KELUAR, "Kendaraan Keluar"));
        sidebar.add(tombolNav(RIWAYAT, "Riwayat Transaksi"));

        sidebar.add(labelGrup("MASTER DATA"));
        sidebar.add(tombolNav(SLOT, "Slot Parkir"));
        sidebar.add(tombolNav(PETUGAS, "Petugas"));
        sidebar.add(tombolNav(TARIF, "Tarif & Denda"));

        sidebar.add(labelGrup("ALAT"));
        sidebar.add(tombolNav(DEMO, "Mode Demo"));

        sidebar.add(Box.createVerticalGlue());

        JLabel versi = new JLabel("v1.0 · Tugas Besar PBO");
        versi.setFont(Tema.FONT_LABEL_KECIL);
        versi.setForeground(new Color(0x6E, 0x7D, 0x99));
        versi.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(versi);

        return sidebar;
    }

    private JPanel buatMerek() {
        JPanel merek = new JPanel(new BorderLayout(12, 0));
        merek.setOpaque(false);
        merek.setAlignmentX(LEFT_ALIGNMENT);
        merek.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        merek.add(new Tema.IkonMerek(42), BorderLayout.WEST);

        JPanel teks = new JPanel();
        teks.setOpaque(false);
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));

        JLabel nama = new JLabel("Eparking");
        nama.setFont(Tema.FONT_JUDUL.deriveFont(19f));
        nama.setForeground(Tema.TEXT);
        nama.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Manajemen Parkir");
        sub.setFont(Tema.FONT_KECIL);
        sub.setForeground(Tema.TEXT_SEKUNDER);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        teks.add(nama);
        teks.add(sub);
        merek.add(teks, BorderLayout.CENTER);
        return merek;
    }

    private JLabel labelGrup(String teks) {
        JLabel label = new JLabel(Tema.renggang(teks));
        label.setFont(Tema.FONT_LABEL_KECIL);
        label.setForeground(new Color(0x78, 0x88, 0xA8));
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(16, 14, 7, 0));
        return label;
    }

    private JButton tombolNav(String kunci, String teks) {
        Tema.TombolNav tombol = new Tema.TombolNav(teks);
        tombol.addActionListener(e -> tampilkan(kunci));
        navTombol.put(kunci, tombol);
        return tombol;
    }

    // -------------------------------------------------------------- kepala

    private JPanel buatKepala() {
        JPanel kepala = new JPanel(new BorderLayout(12, 0));
        kepala.setOpaque(false);
        kepala.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, Tema.BORDER),
                new EmptyBorder(16, 26, 16, 26)));

        JPanel kiri = new JPanel();
        kiri.setOpaque(false);
        kiri.setLayout(new BoxLayout(kiri, BoxLayout.Y_AXIS));

        lblJudul.setFont(Tema.FONT_JUDUL);
        lblJudul.setForeground(Tema.TEXT);
        lblKeterangan.setFont(Tema.FONT_KECIL);
        lblKeterangan.setForeground(Tema.MUTED);
        lblJudul.setAlignmentX(LEFT_ALIGNMENT);
        lblKeterangan.setAlignmentX(LEFT_ALIGNMENT);

        kiri.add(lblJudul);
        kiri.add(Box.createVerticalStrut(3));
        kiri.add(lblKeterangan);
        kepala.add(kiri, BorderLayout.WEST);

        JPanel kanan = new JPanel(new GridBagLayout());
        kanan.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 12);

        gbc.gridx = 0;
        kanan.add(Tema.avatar(petugas.getNama(), 38), gbc);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel kecil = new JLabel("Masuk sebagai");
        kecil.setFont(Tema.FONT_LABEL_KECIL);
        kecil.setForeground(Tema.MUTED);
        kecil.setAlignmentX(LEFT_ALIGNMENT);
        JLabel nama = new JLabel(petugas.getNama());
        nama.setFont(Tema.FONT_TEBAL);
        nama.setForeground(Tema.TEXT);
        nama.setAlignmentX(LEFT_ALIGNMENT);
        info.add(kecil);
        info.add(nama);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 16);
        kanan.add(info, gbc);

        JButton logout = Tema.kedua("Logout");
        logout.addActionListener(e -> logout());
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        kanan.add(logout, gbc);

        kepala.add(kanan, BorderLayout.EAST);
        return kepala;
    }

    // --------------------------------------------------------------- menu

    private JMenuBar buatMenu() {
        JMenuBar bar = new JMenuBar();
        bar.setUI(new javax.swing.plaf.basic.BasicMenuBarUI());
        bar.setBackground(Tema.BG_GELAP);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, Tema.BORDER),
                new EmptyBorder(5, 10, 5, 10)));

        JMenu berkas = menu("Berkas");
        JMenuItem dashboard = menuItem("Dashboard", KeyEvent.VK_D, e -> tampilkan(DASHBOARD));
        JMenuItem logout = menuItem("Logout", KeyEvent.VK_L, e -> logout());
        JMenuItem keluar = menuItem("Keluar Aplikasi", KeyEvent.VK_Q, e -> tutupAplikasi());
        berkas.add(dashboard);
        berkas.addSeparator();
        berkas.add(logout);
        berkas.add(keluar);

        JMenu transaksi = menu("Transaksi");
        transaksi.add(menuItem("Kendaraan Masuk", KeyEvent.VK_1, e -> tampilkan(MASUK)));
        transaksi.add(menuItem("Kendaraan Keluar", KeyEvent.VK_2, e -> tampilkan(KELUAR)));
        transaksi.add(menuItem("Riwayat Transaksi", KeyEvent.VK_3, e -> tampilkan(RIWAYAT)));

        JMenu master = menu("Master Data");
        master.add(menuItem("Slot Parkir", 0, e -> tampilkan(SLOT)));
        master.add(menuItem("Petugas", 0, e -> tampilkan(PETUGAS)));
        master.add(menuItem("Tarif & Denda", 0, e -> tampilkan(TARIF)));

        JMenu alat = menu("Alat");
        alat.add(menuItem("Mode Demo", 0, e -> tampilkan(DEMO)));
        alat.addSeparator();
        alat.add(menuItem("Tentang Aplikasi", 0, e -> tentang()));

        bar.add(berkas);
        bar.add(transaksi);
        bar.add(master);
        bar.add(alat);
        return bar;
    }

    private JMenu menu(String teks) {
        JMenu menu = new JMenu(teks);
        menu.setFont(Tema.FONT_BADAN);
        menu.setForeground(Tema.TEXT_SEKUNDER);
        menu.setOpaque(false);
        menu.setBorder(new EmptyBorder(4, 10, 4, 10));
        return menu;
    }

    private JMenuItem menuItem(String teks, int tombolPintas, java.awt.event.ActionListener aksi) {
        JMenuItem item = new JMenuItem(teks);
        item.setFont(Tema.FONT_BADAN);
        item.setForeground(Tema.TEXT);
        item.setBackground(Tema.PANEL_GELAP);
        item.setOpaque(true);
        item.setBorder(new EmptyBorder(6, 12, 6, 12));
        if (tombolPintas != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(tombolPintas, InputEvent.CTRL_DOWN_MASK));
        }
        item.addActionListener(aksi);
        return item;
    }

    // --------------------------------------------------------------- status

    private JPanel buatStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, Tema.BORDER),
                new EmptyBorder(9, 26, 9, 26)));

        JLabel kiri = new JLabel("Eparking v1.0 · Basis data lokal SQLite · " + petugas.getUsername());
        kiri.setFont(Tema.FONT_KECIL);
        kiri.setForeground(Tema.MUTED);

        lblJam.setFont(Tema.FONT_KECIL);
        lblJam.setForeground(Tema.MUTED);

        bar.add(kiri, BorderLayout.WEST);
        bar.add(lblJam, BorderLayout.EAST);
        return bar;
    }

    private void perbaruiJam() {
        lblJam.setText("Waktu aplikasi: " + Tema.waktu(Jam.sekarang()));
    }

    /** Menyegarkan jam pada status bar, dipakai panel Mode Demo. */
    public void segarkanWaktu() {
        perbaruiJam();
    }

    // ---------------------------------------------------------------- aksi

    private void logout() {
        int pilih = JOptionPane.showConfirmDialog(this,
                "Keluar dari akun " + petugas.getNama() + "?",
                "Konfirmasi Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (pilih == JOptionPane.YES_OPTION) {
            timerJam.stop();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void tutupAplikasi() {
        int pilih = JOptionPane.showConfirmDialog(this,
                "Tutup aplikasi Eparking?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (pilih == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void tentang() {
        String pesan = """
                Eparking v1.0
                Aplikasi manajemen parkir berbasis Java Swing dan SQLite.

                Tugas Besar Pemrograman Berorientasi Objek.
                Data tersimpan lokal pada data/parkir.db.
                """;
        JOptionPane.showMessageDialog(this, pesan, "Tentang Eparking", JOptionPane.INFORMATION_MESSAGE);
    }
}
