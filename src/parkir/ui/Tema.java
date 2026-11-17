package parkir.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import parkir.exception.ValidasiException;

/**
 * Tema visual dan komponen bersama untuk seluruh layar.
 *
 * <p>Warna, font, dan gaya tombol dikumpulkan di satu tempat agar tampilan
 * konsisten antar layar dan mudah diubah.</p>
 */
public final class Tema {

    public static final Color BG = new Color(0xF4F6F5);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SIDEBAR = new Color(0x123B33);
    public static final Color SIDEBAR_HOVER = new Color(0x1B4C42);
    public static final Color PRIMARY = new Color(0x1F6F5C);
    public static final Color PRIMARY_TERANG = new Color(0xDCEDE8);
    public static final Color TEXT = new Color(0x1F2933);
    public static final Color MUTED = new Color(0x66727D);
    public static final Color BORDER = new Color(0xE1E6E4);
    public static final Color DANGER = new Color(0xC0392B);
    public static final Color SUKSES = new Color(0x2E7D32);
    public static final Color BARIS_GANJIL = new Color(0xFAFBFA);

    public static final Font FONT_JUDUL = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_SUBJUDUL = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_BADAN = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_TEBAL = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_KECIL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_ANGKA = new Font("SansSerif", Font.BOLD, 26);
    public static final Font FONT_MONO = new Font("Monospaced", Font.BOLD, 15);
    public static final Font FONT_NAV = new Font("SansSerif", Font.PLAIN, 14);

    public static final DateTimeFormatter FORMAT_WAKTU = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private Tema() {
    }

    /** Menerapkan default komponen Swing agar seragam. */
    public static void pasang() {
        UIManager.put("Label.font", FONT_BADAN);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.messageFont", FONT_BADAN);
        UIManager.put("OptionPane.buttonFont", FONT_BADAN);
        UIManager.put("Button.font", FONT_BADAN);
        UIManager.put("TextField.font", FONT_BADAN);
        UIManager.put("PasswordField.font", FONT_BADAN);
        UIManager.put("ComboBox.font", FONT_BADAN);
        UIManager.put("CheckBox.font", FONT_BADAN);
        UIManager.put("Spinner.font", FONT_BADAN);
        UIManager.put("Table.font", FONT_BADAN);
        UIManager.put("TableHeader.font", FONT_TEBAL);
        UIManager.put("ToolTip.font", FONT_KECIL);
        UIManager.put("ComboBox.background", SURFACE);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", PRIMARY_TERANG);
        UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("List.selectionBackground", PRIMARY_TERANG);
        UIManager.put("List.selectionForeground", TEXT);
        UIManager.put("TextField.selectionBackground", PRIMARY_TERANG);
        UIManager.put("TextField.selectionForeground", TEXT);
        UIManager.put("PasswordField.selectionBackground", PRIMARY_TERANG);
        UIManager.put("PasswordField.selectionForeground", TEXT);
        UIManager.put("Table.selectionBackground", PRIMARY_TERANG);
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("Menu.selectionBackground", PRIMARY_TERANG);
        UIManager.put("MenuItem.selectionBackground", PRIMARY_TERANG);
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
        return areaBungkus(teks, lebar, FONT_BADAN, TEXT);
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
        area.setMargin(new java.awt.Insets(0, 0, 0, 0));
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

    // ------------------------------------------------------------ input

    public static JTextField field(int kolom) {
        JTextField field = new JTextField(kolom);
        field.setFont(FONT_BADAN);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 9, 7, 9)));
        return field;
    }

    public static JPasswordField sandi(int kolom) {
        JPasswordField field = new JPasswordField(kolom);
        field.setFont(FONT_BADAN);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 9, 7, 9)));
        return field;
    }

    // ----------------------------------------------------------- tombol

    public static JButton utama(String teks) {
        return tombol(teks, PRIMARY, Color.WHITE);
    }

    public static JButton kedua(String teks) {
        return tombol(teks, new Color(0xEDF1EF), TEXT);
    }

    public static JButton bahaya(String teks) {
        return tombol(teks, new Color(0xFBEAE8), DANGER);
    }

    private static JButton tombol(String teks, Color latar, Color teksWarna) {
        JButton tombol = new JButton(teks);
        tombol.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        tombol.setFont(FONT_TEBAL);
        tombol.setFocusPainted(false);
        tombol.setBorderPainted(false);
        tombol.setOpaque(true);
        tombol.setBorder(new EmptyBorder(9, 16, 9, 16));
        tombol.addPropertyChangeListener("enabled", e -> warnaiTombol(tombol, latar, teksWarna));
        warnaiTombol(tombol, latar, teksWarna);
        return tombol;
    }

    private static void warnaiTombol(JButton tombol, Color latar, Color teksWarna) {
        boolean aktif = tombol.isEnabled();
        tombol.setBackground(aktif ? latar : new Color(0xE9EDEB));
        tombol.setForeground(aktif ? teksWarna : new Color(0xA0AAA6));
        tombol.setCursor(Cursor.getPredefinedCursor(
                aktif ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    // ------------------------------------------------------------ panel

    /** Kartu putih dengan garis tipis dan padding dalam. */
    public static JPanel kartu() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 18, 16, 18)));
        return panel;
    }

    public static JPanel kartuStatistik(String judul, JLabel nilai, String catatan) {
        JPanel panel = kartu();
        panel.setLayout(new java.awt.BorderLayout(0, 4));
        panel.add(kecil(judul.toUpperCase()), java.awt.BorderLayout.NORTH);
        panel.add(nilai, java.awt.BorderLayout.CENTER);
        panel.add(kecil(catatan), java.awt.BorderLayout.SOUTH);
        return panel;
    }

    /** Baris horizontal dengan jarak antar komponen. */
    public static JPanel baris(Component... komponen) {
        JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
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
            panel.add(javax.swing.Box.createVerticalStrut(8));
        }
        return panel;
    }

    /** Label kecil di atas komponen input, rata kiri dan selebar formulir. */
    public static JPanel berlabel(String label, Component input) {
        JPanel panel = new JPanel(new java.awt.BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel teks = kecil(label);
        teks.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(teks, java.awt.BorderLayout.NORTH);

        if (input instanceof javax.swing.JComponent) {
            javax.swing.JComponent komponen = (javax.swing.JComponent) input;
            komponen.setAlignmentX(Component.LEFT_ALIGNMENT);
            komponen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
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
        JTable tabel = new JTable(model) {
            private static final long serialVersionUID = 1L;

            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int baris, int kolomTabel) {
                Component komponen = super.prepareRenderer(renderer, baris, kolomTabel);
                if (!isRowSelected(baris)) {
                    komponen.setBackground(baris % 2 == 0 ? SURFACE : BARIS_GANJIL);
                    komponen.setForeground(TEXT);
                }
                return komponen;
            }
        };
        tabel.setRowHeight(30);
        tabel.setShowVerticalLines(false);
        tabel.setShowHorizontalLines(true);
        tabel.setGridColor(new Color(0xEFF2F1));
        tabel.setIntercellSpacing(new Dimension(0, 1));
        tabel.setSelectionBackground(PRIMARY_TERANG);
        tabel.setSelectionForeground(TEXT);
        tabel.setFillsViewportHeight(true);
        tabel.setAutoCreateRowSorter(true);

        JTableHeader header = tabel.getTableHeader();
        header.setFont(FONT_TEBAL);
        header.setBackground(new Color(0xF0F3F2));
        header.setForeground(MUTED);
        header.setPreferredSize(new Dimension(0, 34));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setReorderingAllowed(false);
        return tabel;
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

    /** Membungkus komponen (biasanya tabel) dengan scroll dan garis tipis. */
    public static JScrollPane gulir(Component isi) {
        JScrollPane scroll = new JScrollPane(isi);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(SURFACE);
        return scroll;
    }

    /** Indeks baris terpilih yang sudah disesuaikan dengan pengurutan tabel. */
    public static int barisTerpilih(JTable tabel) {
        int baris = tabel.getSelectedRow();
        return baris < 0 ? -1 : tabel.convertRowIndexToModel(baris);
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

    /** Komponen teks berkode warna untuk angka penting. */
    public static JLabel nilaiBesar(String teks, Color warna) {
        JLabel label = new JLabel(teks);
        label.setFont(FONT_ANGKA);
        label.setForeground(warna);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        return label;
    }
}
