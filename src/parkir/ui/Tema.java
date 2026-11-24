package parkir.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import parkir.exception.ValidasiException;

/**
 * Tema visual dan komponen bersama untuk seluruh layar.
 *
 * <p>Mengikuti bahasa desain "ParkEase": latar navy gelap, kartu kaca
 * transparan dengan sudut membulat, aksen biru, serta tipografi Plus Jakarta
 * Sans (body) dan Inter (label kapital). Semua warna, radius, dan gaya
 * komponen dikumpulkan di sini agar tampilan konsisten antar layar.</p>
 */
public final class Tema {

    // ------------------------------------------------------------ palet

    /** Latar aplikasi. */
    public static final Color BG = new Color(0x0B1220);
    /** Varian latar lebih gelap (sidebar, menu). */
    public static final Color BG_GELAP = new Color(0x0A0F19);
    /** Varian latar paling pekat. */
    public static final Color BG_PEKAT = new Color(0x060810);
    /** Panel gelap transparan (input, popup). */
    public static final Color PANEL_GELAP = new Color(0x0F1622);
    /** Permukaan kartu utama. */
    public static final Color KARTU = new Color(0x16203A);
    /** Permukaan kartu sekunder (header tabel). */
    public static final Color KARTU_2 = new Color(0x1B2434);
    /** Garis/border tipis. */
    public static final Color BORDER = new Color(0x26334F);
    /** Teks primer. */
    public static final Color TEXT = new Color(0xF2F6FF);
    /** Teks sekunder. */
    public static final Color TEXT_SEKUNDER = new Color(0xB7C2D6);
    /** Teks keterangan. */
    public static final Color MUTED = new Color(0xA6B2C6);
    /** Aksen biru. */
    public static final Color PRIMARY = new Color(0x1856FF);
    public static final Color PRIMARY_HOVER = new Color(0x4C7DFF);
    public static final Color PRIMARY_TEKAN = new Color(0x1244CC);
    /** Aksen status. */
    public static final Color SUKSES = new Color(0x07CA6B);
    public static final Color PERINGATAN = new Color(0xF59E0B);
    public static final Color UNGU = new Color(0xA855F7);
    public static final Color CYAN = new Color(0x22D3EE);
    /** Nada galat turunan agar pesan kesalahan tetap terbaca di latar gelap. */
    public static final Color DANGER = new Color(0xF87171);
    /** Latar baris terpilih (biru aksen di atas navy). */
    public static final Color SELEKSI = new Color(0x1C3B6E);

    // Nama lama tetap dipertahankan agar panel lain tidak perlu diubah total.
    public static final Color SURFACE = KARTU;
    public static final Color SIDEBAR = BG_GELAP;
    public static final Color SIDEBAR_HOVER = KARTU;
    public static final Color PRIMARY_TERANG = SELEKSI;
    public static final Color BARIS_GANJIL = PANEL_GELAP;

    public static final int RADIUS_INPUT = 8;
    public static final int RADIUS_KARTU = 12;
    public static final int RADIUS_PANEL = 16;

    // ------------------------------------------------------------ font

    private static final Font BODY_REG;
    private static final Font BODY_MED;
    private static final Font BODY_SEMI;
    private static final Font BODY_BOLD;
    private static final Font LABEL_REG;
    private static final Font LABEL_MED;
    private static final Font LABEL_SEMI;
    private static final Font LABEL_BOLD;
    private static final String FAMILI_BODY;
    private static final String FAMILI_LABEL;
    /** True bila Plus Jakarta Sans dan Inter berhasil dimuat dari lib/fonts. */
    public static final boolean FONT_TERBUNDEL;

    static {
        BODY_REG = muatFont("PlusJakartaSans-Regular.ttf");
        BODY_MED = muatFont("PlusJakartaSans-Medium.ttf");
        BODY_SEMI = muatFont("PlusJakartaSans-SemiBold.ttf");
        BODY_BOLD = muatFont("PlusJakartaSans-Bold.ttf");
        LABEL_REG = muatFont("Inter-Regular.ttf");
        LABEL_MED = muatFont("Inter-Medium.ttf");
        LABEL_SEMI = muatFont("Inter-SemiBold.ttf");
        LABEL_BOLD = muatFont("Inter-Bold.ttf");

        boolean bodySiap = BODY_REG != null && BODY_MED != null;
        boolean labelSiap = LABEL_REG != null && LABEL_SEMI != null;
        FONT_TERBUNDEL = bodySiap && labelSiap;
        FAMILI_BODY = bodySiap ? BODY_REG.getFamily() : keluargaTersedia("Segoe UI", "Dialog");
        FAMILI_LABEL = labelSiap ? LABEL_SEMI.getFamily()
                : keluargaTersedia("Segoe UI Semibold", "Segoe UI", "Dialog");
    }

    public static final Font FONT_JUDUL = badan(24f, 700);
    public static final Font FONT_SUBJUDUL = badan(16f, 600);
    public static final Font FONT_BADAN = badan(13f, 400);
    public static final Font FONT_TEBAL = badan(13f, 600);
    public static final Font FONT_KECIL = badan(11.5f, 400);
    public static final Font FONT_ANGKA = badan(28f, 700);
    public static final Font FONT_MONO = fontMono();
    public static final Font FONT_NAV = badan(13.5f, 500);
    /** Label kapital Inter semibold dengan spasi huruf. */
    public static final Font FONT_LABEL = label(10.5f, 600);
    public static final Font FONT_LABEL_KECIL = label(9.5f, 600);
    public static final Font FONT_BADGE = badan(11f, 600);

    public static final DateTimeFormatter FORMAT_WAKTU = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private Tema() {
    }

    private static Font muatFont(String nama) {
        String[] folder = { "lib/fonts", "fonts", "../lib/fonts" };
        for (String dir : folder) {
            File berkas = new File(dir, nama);
            if (!berkas.isFile()) {
                continue;
            }
            try {
                Font font = Font.createFont(Font.TRUETYPE_FONT, berkas);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                return font;
            } catch (FontFormatException | IOException e) {
                // Coba kandidat folder berikutnya.
            }
        }
        return null;
    }

    private static String keluargaTersedia(String... kandidat) {
        Set<String> tersedia = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String nama : kandidat) {
            if (tersedia.contains(nama)) {
                return nama;
            }
        }
        return Font.SANS_SERIF;
    }

    private static Font badan(float ukuran, int bobot) {
        Font dasar = switch (bobot) {
            case 500 -> BODY_MED;
            case 600 -> BODY_SEMI;
            case 700 -> BODY_BOLD;
            default -> BODY_REG;
        };
        if (dasar != null) {
            return dasar.deriveFont(ukuran);
        }
        return new Font(FAMILI_BODY, bobot >= 600 ? Font.BOLD : Font.PLAIN, 1).deriveFont(ukuran);
    }

    private static Font label(float ukuran, int bobot) {
        Font dasar = switch (bobot) {
            case 500 -> LABEL_MED;
            case 600 -> LABEL_SEMI;
            case 700 -> LABEL_BOLD;
            default -> LABEL_REG;
        };
        if (dasar != null) {
            return dasar.deriveFont(ukuran);
        }
        return new Font(FAMILI_LABEL, bobot >= 600 ? Font.BOLD : Font.PLAIN, 1).deriveFont(ukuran);
    }

    private static Font fontMono() {
        Set<String> tersedia = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        String nama = tersedia.contains("Consolas") ? "Consolas" : Font.MONOSPACED;
        return new Font(nama, Font.PLAIN, 13);
    }

    /** Menambahkan spasi huruf tipis agar label kapital terasa seperti desain. */
    public static String renggang(String teks) {
        if (teks == null || teks.length() < 2) {
            return teks;
        }
        StringBuilder hasil = new StringBuilder(teks.length() * 2);
        for (int i = 0; i < teks.length(); i++) {
            char c = teks.charAt(i);
            if (c == ' ') {
                hasil.append('\u2002');
                continue;
            }
            hasil.append(c);
            if (i < teks.length() - 1 && teks.charAt(i + 1) != ' '
                    && ".,:;!?)]}%".indexOf(teks.charAt(i + 1)) < 0) {
                hasil.append('\u2009');
            }
        }
        return hasil.toString();
    }

    /** Menerapkan default komponen Swing agar seragam. */
    public static void pasang() {
        UIManager.put("Label.font", FONT_BADAN);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", KARTU);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.messageFont", FONT_BADAN);
        UIManager.put("OptionPane.buttonFont", FONT_TEBAL);
        UIManager.put("Button.font", FONT_TEBAL);
        UIManager.put("Button.background", KARTU_2);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.select", PRIMARY);
        UIManager.put("TextField.font", FONT_BADAN);
        UIManager.put("TextField.background", PANEL_GELAP);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("PasswordField.font", FONT_BADAN);
        UIManager.put("PasswordField.background", PANEL_GELAP);
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("PasswordField.caretForeground", TEXT);
        UIManager.put("ComboBox.font", FONT_BADAN);
        UIManager.put("ComboBox.background", PANEL_GELAP);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("CheckBox.font", FONT_BADAN);
        UIManager.put("CheckBox.foreground", TEXT_SEKUNDER);
        UIManager.put("Spinner.font", FONT_BADAN);
        UIManager.put("Table.font", FONT_BADAN);
        UIManager.put("Table.background", KARTU);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TableHeader.font", FONT_LABEL);
        UIManager.put("TableHeader.background", KARTU_2);
        UIManager.put("TableHeader.foreground", TEXT_SEKUNDER);
        UIManager.put("ToolTip.font", FONT_KECIL);
        UIManager.put("ToolTip.background", KARTU_2);
        UIManager.put("ToolTip.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", PRIMARY);
        UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("List.selectionBackground", PRIMARY);
        UIManager.put("List.selectionForeground", TEXT);
        UIManager.put("List.background", PANEL_GELAP);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("TextField.selectionBackground", PRIMARY);
        UIManager.put("TextField.selectionForeground", TEXT);
        UIManager.put("PasswordField.selectionBackground", PRIMARY);
        UIManager.put("PasswordField.selectionForeground", TEXT);
        UIManager.put("Table.selectionBackground", SELEKSI);
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("Menu.selectionBackground", KARTU_2);
        UIManager.put("Menu.selectionForeground", TEXT);
        UIManager.put("MenuItem.selectionBackground", KARTU_2);
        UIManager.put("MenuItem.selectionForeground", TEXT);
        UIManager.put("MenuBar.background", BG_GELAP);
        UIManager.put("MenuBar.foreground", TEXT_SEKUNDER);
        UIManager.put("Menu.background", BG_GELAP);
        UIManager.put("Menu.foreground", TEXT_SEKUNDER);
        UIManager.put("MenuItem.background", PANEL_GELAP);
        UIManager.put("MenuItem.foreground", TEXT);
        UIManager.put("PopupMenu.background", PANEL_GELAP);
        UIManager.put("PopupMenu.foreground", TEXT);
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(BORDER));
        UIManager.put("Separator.foreground", BORDER);
        UIManager.put("ScrollPane.background", KARTU);
        UIManager.put("Viewport.background", KARTU);
        UIManager.put("FileChooser.background", KARTU);
        UIManager.put("FileChooser.listFont", FONT_BADAN);
        UIManager.put("Tree.background", PANEL_GELAP);
        UIManager.put("Tree.foreground", TEXT);
        UIManager.put("ToolBar.background", KARTU);
    }

    // ------------------------------------------------------------- teks

    public static JLabel judul(String teks) {
        JLabel label = new JLabel(teks);
        label.setFont(FONT_JUDUL);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel subjudul(String teks) {
        JLabel label = new JLabel(teks);
        label.setFont(FONT_SUBJUDUL);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel teks(String teks) {
        JLabel label = new JLabel(teks);
        label.setFont(FONT_BADAN);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel kecil(String teks) {
        JLabel label = new JLabel(teks);
        label.setFont(FONT_KECIL);
        label.setForeground(MUTED);
        return label;
    }

    /** Label section kapital Inter semibold dengan spasi huruf. */
    public static JLabel labelKapital(String teks) {
        JLabel label = new JLabel(renggang(teks == null ? "" : teks.toUpperCase()));
        label.setFont(FONT_LABEL);
        label.setForeground(MUTED);
        return label;
    }

    /** Pil status kecil (badge) dengan latar transparan berwarna aksen. */
    public static JLabel badge(String teks, Color warna) {
        JLabel label = new JLabel(teks) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(warna.getRed(), warna.getGreen(), warna.getBlue(), 38));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setOpaque(false);
        label.setFont(FONT_BADGE);
        label.setForeground(warna);
        label.setBorder(new EmptyBorder(4, 11, 4, 11));
        return label;
    }

    /**
     * Catatan kecil yang membungkus teks otomatis pada lebar tertentu.
     *
     * <p>Dipakai untuk keterangan panjang di dalam kartu agar tidak terpotong
     * ketika jendela berubah ukuran.</p>
     */
    public static javax.swing.JTextArea catatan(String teks, int lebar) {
        return areaBungkus(teks, lebar, FONT_KECIL, MUTED);
    }

    /** Paragraf berukuran normal yang membungkus teks otomatis. */
    public static javax.swing.JTextArea paragraf(String teks, int lebar) {
        return areaBungkus(teks, lebar, FONT_BADAN, TEXT_SEKUNDER);
    }

    private static javax.swing.JTextArea areaBungkus(String teks, int lebar, Font font, Color warna) {
        javax.swing.JTextArea area = new javax.swing.JTextArea(teks);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(font);
        area.setForeground(warna);
        area.setBorder(null);
        area.setMargin(new Insets(0, 0, 0, 0));
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        area.setSize(new Dimension(lebar, Integer.MAX_VALUE));
        Dimension ukuran = area.getPreferredSize();
        area.setPreferredSize(new Dimension(lebar, ukuran.height));
        area.setMaximumSize(new Dimension(Integer.MAX_VALUE, ukuran.height));
        return area;
    }

    public static JLabel angka(String teks) {
        JLabel label = new JLabel(teks);
        label.setFont(FONT_ANGKA);
        label.setForeground(PRIMARY);
        return label;
    }

    /** Komponen teks berkode warna untuk angka penting. */
    public static JLabel nilaiBesar(String teks, Color warna) {
        JLabel label = new JLabel(teks);
        label.setFont(FONT_ANGKA);
        label.setForeground(warna);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        return label;
    }

    // ------------------------------------------------------------ input

    public static JTextField field(int kolom) {
        JTextField field = new Kotak(kolom);
        gayaInput(field);
        return field;
    }

    public static JPasswordField sandi(int kolom) {
        JPasswordField field = new KotakSandi(kolom);
        gayaInput(field);
        return field;
    }

    public static void gayaInput(JTextField field) {
        field.setOpaque(false);
        field.setFont(FONT_BADAN);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setSelectionColor(PRIMARY);
        field.setSelectedTextColor(TEXT);
        // Latar digambar oleh paintComponent kotak; border hanya garis tepi.
        field.setBorder(new BorderBulat(null, BORDER, RADIUS_INPUT, new Insets(9, 12, 9, 12)));
    }

    /** Latar membulat untuk komponen input. */
    static void latarBulat(Graphics g, JComponent komponen) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(PANEL_GELAP);
        g2.fillRoundRect(0, 0, komponen.getWidth() - 1, komponen.getHeight() - 1,
                RADIUS_INPUT, RADIUS_INPUT);
        g2.dispose();
    }

    /** Text field dengan latar membulat yang digambar sebelum teks. */
    public static class Kotak extends JTextField {

        private static final long serialVersionUID = 1L;

        public Kotak(int kolom) {
            super(kolom);
        }

        @Override
        protected void paintComponent(Graphics g) {
            latarBulat(g, this);
            super.paintComponent(g);
        }
    }

    /** Password field dengan latar membulat yang digambar sebelum teks. */
    public static class KotakSandi extends JPasswordField {

        private static final long serialVersionUID = 1L;

        public KotakSandi(int kolom) {
            super(kolom);
        }

        @Override
        protected void paintComponent(Graphics g) {
            latarBulat(g, this);
            super.paintComponent(g);
        }
    }

    // ----------------------------------------------------------- tombol

    public static JButton utama(String teks) {
        return new Tombol(teks, PRIMARY, PRIMARY_HOVER, PRIMARY_TEKAN, null, TEXT);
    }

    public static JButton kedua(String teks) {
        return new Tombol(teks, new Color(0, 0, 0, 0), KARTU_2, new Color(0x20, 0x2B, 0x45),
                BORDER, TEXT_SEKUNDER);
    }

    public static JButton bahaya(String teks) {
        return new Tombol(teks, new Color(0, 0, 0, 0),
                new Color(0xF5, 0x9E, 0x0B, 28), new Color(0xF5, 0x9E, 0x0B, 46),
                new Color(0xF5, 0x9E, 0x0B, 120), PERINGATAN);
    }

    /** Tombol dengan sudut membulat, isi berlapis, dan status hover. */
    public static class Tombol extends JButton {

        private static final long serialVersionUID = 1L;

        private final Color isi;
        private final Color isiHover;
        private final Color isiTekan;
        private final Color garis;
        private final Color teksWarna;

        Tombol(String teks, Color isi, Color isiHover, Color isiTekan, Color garis, Color teksWarna) {
            super(teks);
            this.isi = isi;
            this.isiHover = isiHover;
            this.isiTekan = isiTekan;
            this.garis = garis;
            this.teksWarna = teksWarna;

            setUI(new BasicButtonUI());
            setFont(FONT_TEBAL);
            setForeground(teksWarna);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 18, 9, 18));
            getModel().addChangeListener(e -> repaint());
            addPropertyChangeListener("enabled", e -> perbaruiWarna());
        }

        private void perbaruiWarna() {
            boolean aktif = isEnabled();
            setForeground(aktif ? teksWarna : new Color(0x7C, 0x8A, 0xA6));
            setCursor(Cursor.getPredefinedCursor(aktif ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color latar = isi;
            if (!isEnabled()) {
                latar = new Color(0x14, 0x1B, 0x2D);
            } else if (getModel().isPressed()) {
                latar = isiTekan;
            } else if (getModel().isRollover()) {
                latar = isiHover;
            }
            if (latar.getAlpha() > 0) {
                g2.setColor(latar);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS_INPUT, RADIUS_INPUT);
            }
            Color tepi = isEnabled() ? garis : new Color(0x22, 0x2C, 0x44);
            if (tepi != null) {
                g2.setColor(tepi);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS_INPUT, RADIUS_INPUT);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Item navigasi sidebar berupa pil; aktif berarti terisi aksen biru. */
    public static class TombolNav extends JButton {

        private static final long serialVersionUID = 1L;

        private boolean aktif;

        public TombolNav(String teks) {
            super(teks);
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(FONT_NAV);
            setForeground(TEXT_SEKUNDER);
            setBorder(new EmptyBorder(9, 14, 9, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            getModel().addChangeListener(e -> repaint());
        }

        public void setAktif(boolean aktif) {
            this.aktif = aktif;
            setForeground(aktif ? TEXT : TEXT_SEKUNDER);
            setFont(aktif ? badan(13.5f, 600) : FONT_NAV);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (aktif) {
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS_INPUT, RADIUS_INPUT);
            } else if (getModel().isRollover()) {
                g2.setColor(KARTU);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS_INPUT, RADIUS_INPUT);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ------------------------------------------------------------ panel

    /** Kartu kaca dengan sudut membulat, garis tipis, dan padding dalam. */
    public static JPanel kartu() {
        return new Kartu(RADIUS_KARTU, new Color(0x16, 0x20, 0x3A, 0xEE), BORDER, new Insets(18, 20, 18, 20));
    }

    /** Kartu besar untuk formulir utama (radius 16). */
    public static JPanel kartuBesar() {
        return new Kartu(RADIUS_PANEL, new Color(0x16, 0x20, 0x3A, 0xF2), BORDER, new Insets(26, 28, 26, 28));
    }

    /** Kotak kecil di dalam kartu (mis. petunjuk bantuan). */
    public static JPanel panelLembut() {
        return new Kartu(10, new Color(0x0F, 0x16, 0x22, 0xC0), BORDER, new Insets(12, 14, 12, 14));
    }

    /** Panel berlatar gradien navy dengan cahaya biru/ungu lembut. */
    public static class Latar extends JPanel {

        private static final long serialVersionUID = 1L;

        public Latar() {
            this(new java.awt.BorderLayout());
        }

        public Latar(LayoutManager layout) {
            super(layout);
            setOpaque(true);
            setBackground(BG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(new GradientPaint(0, 0, BG_GELAP, w, h, BG));
            g2.fillRect(0, 0, w, h);
            cahaya(g2, w * 0.82f, h * 0.10f, Math.max(w, h) * 0.55f, new Color(0x18, 0x56, 0xFF, 46));
            cahaya(g2, w * 0.10f, h * 0.90f, Math.max(w, h) * 0.50f, new Color(0xA8, 0x55, 0xF7, 32));
            cahaya(g2, w * 0.55f, h * 0.02f, Math.max(w, h) * 0.35f, new Color(0x22, 0xD3, 0xEE, 18));
            g2.dispose();
            // Sengaja tidak memanggil super.paintComponent agar gradien tidak ditimpa.
        }

        private void cahaya(Graphics2D g2, float cx, float cy, float radius, Color warna) {
            g2.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), Math.max(radius, 1f),
                    new float[] { 0f, 1f },
                    new Color[] { warna,
                            new Color(warna.getRed(), warna.getGreen(), warna.getBlue(), 0) }));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public static JPanel latar() {
        return new Latar();
    }

    public static JPanel latar(LayoutManager layout) {
        return new Latar(layout);
    }

    /** Kartu dengan latar membulat dan garis tipis. */
    public static class Kartu extends JPanel {

        private static final long serialVersionUID = 1L;

        private final int radius;
        private final Color isi;
        private final Color garis;

        public Kartu(int radius, Color isi, Color garis, Insets padding) {
            this.radius = radius;
            this.isi = isi;
            this.garis = garis;
            setOpaque(false);
            setBorder(new EmptyBorder(padding));
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 3; i >= 1; i--) {
                g2.setColor(new Color(0, 0, 0, 9 + i * 4));
                g2.fillRoundRect(i, i + 2, w - i * 2 - 1, h - i * 2 - 1, radius + i * 2, radius + i * 2);
            }
            g2.setColor(isi);
            g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);
            g2.setColor(new Color(255, 255, 255, 16));
            g2.drawLine(radius / 2, 1, w - radius / 2, 1);
            g2.setColor(garis);
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
            g2.dispose();
        }
    }

    public static JPanel kartuStatistik(String judul, JLabel nilai, String catatan) {
        JPanel panel = new Kartu(RADIUS_KARTU, new Color(0x16, 0x20, 0x3A, 0xEE), BORDER,
                new Insets(16, 18, 16, 18));
        panel.setLayout(new java.awt.BorderLayout(0, 6));
        panel.add(labelKapital(judul), java.awt.BorderLayout.NORTH);
        nilai.setFont(FONT_ANGKA);
        panel.add(nilai, java.awt.BorderLayout.CENTER);
        panel.add(kecil(catatan), java.awt.BorderLayout.SOUTH);
        return panel;
    }

    /** Baris horizontal dengan jarak antar komponen. */
    public static JPanel baris(Component... komponen) {
        JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (Component komponen1 : komponen) {
            panel.add(komponen1);
        }
        return panel;
    }

    /** Kolom vertikal dengan jarak tetap. */
    public static JPanel kolom(Component... komponen) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (Component komponen1 : komponen) {
            panel.add(komponen1);
            panel.add(javax.swing.Box.createVerticalStrut(10));
        }
        return panel;
    }

    /** Label kecil di atas komponen input, rata kiri dan selebar formulir. */
    public static JPanel berlabel(String label, Component input) {
        JPanel panel = new JPanel(new java.awt.BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

        JLabel teks = labelKapital(label);
        teks.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(teks, java.awt.BorderLayout.NORTH);

        if (input instanceof JComponent) {
            JComponent komponen = (JComponent) input;
            komponen.setAlignmentX(Component.LEFT_ALIGNMENT);
            komponen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        }
        panel.add(input, java.awt.BorderLayout.CENTER);
        return panel;
    }

    // ----------------------------------------------------------- tabel

    public static JTable tabel(String... kolom) {
        DefaultTableModel model = new DefaultTableModel(kolom, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int baris, int kolomTabel) {
                return false;
            }
        };
        JTable tabel = new JTable(model);
        tabel.setFont(FONT_BADAN);
        tabel.setForeground(TEXT);
        tabel.setBackground(KARTU);
        tabel.setRowHeight(36);
        tabel.setShowVerticalLines(false);
        tabel.setShowHorizontalLines(true);
        tabel.setGridColor(BORDER);
        tabel.setIntercellSpacing(new Dimension(0, 1));
        tabel.setSelectionBackground(SELEKSI);
        tabel.setSelectionForeground(TEXT);
        tabel.setFillsViewportHeight(true);
        tabel.setAutoCreateRowSorter(true);
        tabel.setDefaultRenderer(Object.class, new RenderBaris());

        JTableHeader header = tabel.getTableHeader();
        header.setFont(FONT_LABEL);
        header.setBackground(KARTU_2);
        header.setForeground(TEXT_SEKUNDER);
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderTabel());
        return tabel;
    }

    /** Renderer baris tabel: selang-seling navy, teks terang. */
    public static class RenderBaris extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable tabel, Object nilai, boolean terpilih,
                boolean fokus, int baris, int kolom) {
            super.getTableCellRendererComponent(tabel, nilai, terpilih, fokus, baris, kolom);
            setFont(FONT_BADAN);
            setBorder(new EmptyBorder(0, 9, 0, 9));
            setHorizontalAlignment(SwingConstants.LEFT);
            if (terpilih) {
                setBackground(SELEKSI);
                setForeground(TEXT);
            } else {
                setBackground(baris % 2 == 0 ? KARTU : PANEL_GELAP);
                setForeground(TEXT);
            }
            return this;
        }
    }

    /** Renderer header tabel: permukaan sekunder dan label Inter. */
    public static class HeaderTabel extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable tabel, Object nilai, boolean terpilih,
                boolean fokus, int baris, int kolom) {
            super.getTableCellRendererComponent(tabel, nilai, terpilih, fokus, baris, kolom);
            setOpaque(true);
            setBackground(KARTU_2);
            setForeground(TEXT_SEKUNDER);
            setFont(FONT_LABEL);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(0, 10, 0, 10)));
            setHorizontalAlignment(SwingConstants.LEFT);
            return this;
        }
    }

    public static void isiTabel(JTable tabel, List<Object[]> data) {
        DefaultTableModel model = (DefaultTableModel) tabel.getModel();
        model.setRowCount(0);
        for (Object[] baris : data) {
            model.addRow(baris);
        }
    }

    public static void kosongkanTabel(JTable tabel) {
        ((DefaultTableModel) tabel.getModel()).setRowCount(0);
    }

    /** Membungkus komponen (biasanya tabel) dengan scroll bertema gelap. */
    public static JScrollPane gulir(Component isi) {
        JScrollPane scroll = new JScrollPane(isi);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(KARTU);
        scroll.setBackground(KARTU);
        for (JScrollBar bar : new JScrollBar[] { scroll.getVerticalScrollBar(), scroll.getHorizontalScrollBar() }) {
            bar.setUI(new ScrollGelap());
            bar.setOpaque(false);
            bar.setBorder(null);
            bar.setUnitIncrement(16);
        }
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        return scroll;
    }

    /** Scrollbar tipis bertema gelap tanpa tombol panah. */
    public static class ScrollGelap extends BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(0x27, 0x34, 0x51);
            trackColor = new Color(0, 0, 0, 0);
        }

        @Override
        protected JButton createDecreaseButton(int orientasi) {
            return tombolKosong();
        }

        @Override
        protected JButton createIncreaseButton(int orientasi) {
            return tombolKosong();
        }

        private JButton tombolKosong() {
            JButton tombol = new JButton();
            Dimension nol = new Dimension(0, 0);
            tombol.setPreferredSize(nol);
            tombol.setMinimumSize(nol);
            tombol.setMaximumSize(nol);
            tombol.setBorder(null);
            tombol.setFocusable(false);
            return tombol;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            // Track transparan agar menyatu dengan kartu.
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? new Color(0x3A, 0x4B, 0x70) : new Color(0x27, 0x34, 0x51));
            int lebar = Math.min(r.width - 4, 7);
            int x = r.x + (r.width - lebar) / 2;
            g2.fillRoundRect(x, r.y + 2, lebar, Math.max(r.height - 4, 8), lebar, lebar);
            g2.dispose();
        }
    }

    /** Indeks baris terpilih yang sudah disesuaikan dengan pengurutan tabel. */
    public static int barisTerpilih(JTable tabel) {
        int baris = tabel.getSelectedRow();
        return baris < 0 ? -1 : tabel.convertRowIndexToModel(baris);
    }

    // ---------------------------------------------------- komponen lain

    /** Menyelaraskan JFileChooser bawaan Swing dengan tema gelap. */
    public static void gayaPemilihBerkas(javax.swing.JFileChooser chooser) {
        chooser.setBackground(KARTU);
        chooser.setForeground(TEXT);
        sisirKomponen(chooser);
    }

    private static void sisirKomponen(Component komponen) {
        if (komponen instanceof JComboBox) {
            gayaCombo((JComboBox<?>) komponen);
            return;
        } else if (komponen instanceof JTextField) {
            JTextField field = (JTextField) komponen;
            field.setOpaque(true);
            field.setBackground(PANEL_GELAP);
            field.setForeground(TEXT);
            field.setCaretColor(TEXT);
            field.setSelectionColor(PRIMARY);
            field.setSelectedTextColor(TEXT);
            field.setBorder(new BorderBulat(null, BORDER, RADIUS_INPUT, new Insets(6, 8, 6, 8)));
        } else if (komponen instanceof javax.swing.JToggleButton) {
            javax.swing.JToggleButton tombol = (javax.swing.JToggleButton) komponen;
            tombol.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI());
            tombol.setOpaque(true);
            tombol.setContentAreaFilled(true);
            tombol.setBackground(KARTU_2);
            tombol.setForeground(TEXT);
            tombol.setFocusPainted(false);
        } else if (komponen instanceof JButton) {
            JButton tombol = (JButton) komponen;
            tombol.setUI(new BasicButtonUI());
            tombol.setOpaque(true);
            tombol.setContentAreaFilled(true);
            tombol.setBackground(KARTU_2);
            tombol.setForeground(TEXT);
            tombol.setFocusPainted(false);
            tombol.setBorder(new EmptyBorder(6, 14, 6, 14));
        } else if (komponen instanceof JLabel) {
            ((JLabel) komponen).setForeground(TEXT);
        } else if (komponen instanceof JList) {
            JList<?> daftar = (JList<?>) komponen;
            daftar.setBackground(PANEL_GELAP);
            daftar.setForeground(TEXT);
            daftar.setSelectionBackground(PRIMARY);
            daftar.setSelectionForeground(TEXT);
        } else if (komponen instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) komponen;
            scroll.setBackground(KARTU);
            scroll.getViewport().setBackground(PANEL_GELAP);
        } else if (komponen instanceof JScrollBar) {
            JScrollBar bar = (JScrollBar) komponen;
            bar.setUI(new ScrollGelap());
            bar.setOpaque(false);
            bar.setBorder(null);
        } else if (komponen instanceof JComponent) {
            ((JComponent) komponen).setBackground(KARTU);
        }
        if (komponen instanceof java.awt.Container) {
            for (Component anak : ((java.awt.Container) komponen).getComponents()) {
                sisirKomponen(anak);
            }
        }
    }

    /** Gaya combo box gelap dengan panah datar. */
    public static void gayaCombo(JComboBox<?> combo) {
        combo.setOpaque(false);
        combo.setFont(FONT_BADAN);
        combo.setForeground(TEXT);
        combo.setBackground(PANEL_GELAP);
        combo.setBorder(new BorderBulat(null, BORDER, RADIUS_INPUT, new Insets(4, 10, 4, 4)));
        combo.setUI(new ComboGelap());
        combo.setRenderer(new RenderCombo(combo.getRenderer()));
    }

    public static class ComboGelap extends BasicComboBoxUI {

        @Override
        protected JButton createArrowButton() {
            JButton tombol = new JButton("\u25BE");
            tombol.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
            tombol.setForeground(MUTED);
            tombol.setBackground(PANEL_GELAP);
            tombol.setOpaque(false);
            tombol.setContentAreaFilled(false);
            tombol.setBorderPainted(false);
            tombol.setFocusable(false);
            tombol.setBorder(new EmptyBorder(0, 4, 0, 8));
            tombol.setPreferredSize(new Dimension(24, 24));
            return tombol;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(PANEL_GELAP);
            g2.fillRoundRect(0, 0, comboBox.getWidth() - 1, comboBox.getHeight() - 1,
                    RADIUS_INPUT, RADIUS_INPUT);
            g2.dispose();
        }
    }

    private static final class RenderCombo implements ListCellRenderer<Object> {

        private final ListCellRenderer<Object> asli;

        @SuppressWarnings("unchecked")
        RenderCombo(ListCellRenderer<?> asli) {
            this.asli = (ListCellRenderer<Object>) asli;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object nilai, int indeks,
                boolean terpilih, boolean fokus) {
            Component komponen = asli.getListCellRendererComponent(list, nilai, indeks, terpilih, fokus);
            komponen.setFont(FONT_BADAN);
            if (terpilih && indeks >= 0) {
                komponen.setBackground(PRIMARY);
                komponen.setForeground(TEXT);
            } else {
                komponen.setBackground(PANEL_GELAP);
                komponen.setForeground(TEXT);
            }
            if (komponen instanceof JComponent) {
                ((JComponent) komponen).setOpaque(true);
            }
            if (komponen instanceof JLabel) {
                ((JLabel) komponen).setBorder(new EmptyBorder(5, 9, 5, 9));
            }
            return komponen;
        }
    }

    /** Gaya checkbox gelap dengan ikon centang gambar sendiri. */
    public static void gayaCek(JCheckBox cek) {
        cek.setOpaque(false);
        cek.setFont(FONT_BADAN);
        cek.setForeground(TEXT_SEKUNDER);
        cek.setFocusPainted(false);
        cek.setIcon(new IkonCek(false));
        cek.setSelectedIcon(new IkonCek(true));
        cek.setIconTextGap(8);
        cek.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static final class IkonCek implements Icon {

        private final boolean terpilih;

        IkonCek(boolean terpilih) {
            this.terpilih = terpilih;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(terpilih ? PRIMARY : PANEL_GELAP);
            g2.fillRoundRect(1, 1, 14, 14, 5, 5);
            g2.setColor(terpilih ? PRIMARY : BORDER);
            g2.drawRoundRect(1, 1, 14, 14, 5, 5);
            if (terpilih) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawPolyline(new int[] { 4, 7, 12 }, new int[] { 8, 11, 5 }, 3);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    /** Gaya spinner gelap dengan tombol panah datar. */
    public static void gayaSpinner(JSpinner spinner) {
        spinner.setOpaque(false);
        spinner.setBorder(new BorderBulat(PANEL_GELAP, BORDER, RADIUS_INPUT, new Insets(3, 4, 3, 4)));
        spinner.setUI(new SpinnerGelap());
        if (spinner.getEditor() instanceof JComponent) {
            ((JComponent) spinner.getEditor()).setOpaque(false);
        }
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
            JTextField field = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            field.setOpaque(false);
            field.setForeground(TEXT);
            field.setCaretColor(TEXT);
            field.setFont(FONT_BADAN);
            field.setHorizontalAlignment(JTextField.LEFT);
            field.setBorder(new EmptyBorder(5, 8, 5, 4));
            field.setSelectionColor(PRIMARY);
            field.setSelectedTextColor(TEXT);
        }
    }

    public static class SpinnerGelap extends BasicSpinnerUI {

        @Override
        protected Component createNextButton() {
            return panah("\u25B4", true);
        }

        @Override
        protected Component createPreviousButton() {
            return panah("\u25BE", false);
        }

        private Component panah(String glyph, boolean naik) {
            JButton tombol = new JButton(glyph);
            tombol.setFont(FONT_BADAN.deriveFont(9f));
            tombol.setForeground(TEXT_SEKUNDER);
            tombol.setOpaque(false);
            tombol.setContentAreaFilled(false);
            tombol.setBorderPainted(false);
            tombol.setFocusable(false);
            tombol.setBorder(new EmptyBorder(0, 0, 0, 6));
            tombol.setPreferredSize(new Dimension(22, 0));
            if (naik) {
                installNextButtonListeners(tombol);
            } else {
                installPreviousButtonListeners(tombol);
            }
            return tombol;
        }
    }

    /** Kotak input membulat: mengisi latar dan menggambar garis tepi. */
    public static class BorderBulat extends AbstractBorder {

        private static final long serialVersionUID = 1L;

        private final Color isi;
        private final Color garis;
        private final int radius;
        private final Insets padding;

        public BorderBulat(Color isi, Color garis, int radius, Insets padding) {
            this.isi = isi;
            this.garis = garis;
            this.radius = radius;
            this.padding = padding;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isi != null && isi.getAlpha() > 0) {
                g2.setColor(isi);
                g2.fillRoundRect(x, y, w - 1, h - 1, radius, radius);
            }
            if (garis != null) {
                g2.setColor(c != null && c.hasFocus() ? PRIMARY : garis);
                g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            }
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(padding.top, padding.left, padding.bottom, padding.right);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(padding.top, padding.left, padding.bottom, padding.right);
            return insets;
        }
    }

    /** Logo vektor Eparking: kotak membulat gradien dengan huruf P. */
    public static class IkonMerek extends JLabel {

        private static final long serialVersionUID = 1L;

        private final int ukuran;

        public IkonMerek(int ukuran) {
            this.ukuran = ukuran;
            setOpaque(false);
            setPreferredSize(new Dimension(ukuran, ukuran));
            setMinimumSize(new Dimension(ukuran, ukuran));
            setMaximumSize(new Dimension(ukuran, ukuran));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, PRIMARY_HOVER, ukuran, ukuran, UNGU));
            g2.fillRoundRect(0, 0, ukuran - 1, ukuran - 1, 12, 12);
            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawRoundRect(0, 0, ukuran - 1, ukuran - 1, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(Math.max(2f, ukuran * 0.11f), BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            double x = ukuran * 0.34;
            double y = ukuran * 0.24;
            double tinggi = ukuran * 0.52;
            double lebarLengkung = ukuran * 0.34;
            g2.drawLine((int) Math.round(x), (int) Math.round(y), (int) Math.round(x),
                    (int) Math.round(y + tinggi));
            g2.draw(new Arc2D.Double(x - lebarLengkung / 2, y, lebarLengkung, tinggi * 0.56, 90, -180,
                    Arc2D.OPEN));
            g2.dispose();
        }
    }

    /** Avatar bulat berisi inisial nama petugas. */
    public static JLabel avatar(String nama, int ukuran) {
        String inisial = nama == null || nama.isBlank() ? "?" : nama.trim().substring(0, 1).toUpperCase();
        JLabel label = new JLabel("") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, PRIMARY, w, h, UNGU));
                g2.fillOval(0, 0, w, h);
                g2.setColor(new Color(255, 255, 255, 46));
                g2.drawOval(0, 0, w - 1, h - 1);

                // Inisial digambar manual supaya benar-benar di tengah lingkaran:
                // perataan bawaan JLabel memakai kotak teks, bukan kotak glyph.
                Font f = getFont();
                GlyphVector gv = f.createGlyphVector(g2.getFontRenderContext(), inisial);
                Rectangle2D tinta = gv.getVisualBounds();
                float x = (float) ((w - tinta.getWidth()) / 2f - tinta.getX());
                float y = (float) ((h - tinta.getHeight()) / 2f - tinta.getY());
                g2.setFont(f);
                g2.setColor(getForeground());
                g2.drawGlyphVector(gv, x, y);
                g2.dispose();
            }
        };
        label.setOpaque(false);
        label.setFont(badan(13f, 600));
        label.setForeground(TEXT);
        Dimension dimensi = new Dimension(ukuran, ukuran);
        label.setPreferredSize(dimensi);
        label.setMinimumSize(dimensi);
        label.setMaximumSize(dimensi);
        return label;
    }

    // ----------------------------------------------------------- format

    /**
     * Membaca input uang yang longgar ("4000", "4.000", "Rp 4.000").
     *
     * @throws ValidasiException bila kosong, negatif, atau bukan angka
     */
    public static long bacaUang(String teks) throws ValidasiException {
        if (teks == null || teks.isBlank()) {
            throw new ValidasiException("Nilai uang wajib diisi.");
        }
        if (teks.contains("-")) {
            throw new ValidasiException("Nilai uang tidak boleh negatif.");
        }
        String bersih = teks.replaceAll("[^0-9]", "");
        if (bersih.isEmpty()) {
            throw new ValidasiException("Nilai uang harus berupa angka.");
        }
        try {
            return Long.parseLong(bersih);
        } catch (NumberFormatException e) {
            throw new ValidasiException("Nilai uang terlalu besar.");
        }
    }

    public static String waktu(LocalDateTime waktu) {
        return waktu == null ? "-" : waktu.format(FORMAT_WAKTU);
    }
}
