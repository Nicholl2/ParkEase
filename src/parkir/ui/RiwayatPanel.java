package parkir.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import parkir.exception.DataAccessException;
import parkir.exception.ValidasiException;
import parkir.model.JenisKendaraan;
import parkir.model.StatusTiket;
import parkir.model.Tiket;
import parkir.util.Money;

/** Riwayat transaksi dengan pencarian, filter, detail, dan unduh bukti. */
public class RiwayatPanel extends JPanel implements MuatUlang {

    private static final long serialVersionUID = 1L;

    private final MainFrame frame;

    private final JTextField txtCari = Tema.field(18);
    private final JComboBox<Object> cbStatus = new JComboBox<>();
    private final JComboBox<Object> cbJenis = new JComboBox<>();
    private final JButton btnCari = Tema.utama("Cari");
    private final JButton btnReset = Tema.kedua("Reset");
    private final JButton btnUnduh = Tema.kedua("Unduh Bukti");
    private final JLabel lblInfo = Tema.kecil("");

    private final JTable tabel = Tema.tabel("Nomor Tiket", "Slot", "Jenis", "Masuk", "Keluar",
            "Durasi", "Total", "Status", "Petugas");
    private List<Tiket> data = new ArrayList<>();

    public RiwayatPanel(MainFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        cbStatus.addItem("Semua status");
        for (StatusTiket status : StatusTiket.values()) {
            cbStatus.addItem(status);
        }
        cbJenis.addItem("Semua jenis");
        for (JenisKendaraan jenis : JenisKendaraan.values()) {
            cbJenis.addItem(jenis);
        }
        txtCari.setPreferredSize(new Dimension(190, 34));
        cbStatus.setPreferredSize(new Dimension(150, 34));
        cbJenis.setPreferredSize(new Dimension(150, 34));

        Tema.gayaCombo(cbStatus);
        Tema.gayaCombo(cbJenis);

        add(buatFilter(), BorderLayout.NORTH);
        add(buatTabel(), BorderLayout.CENTER);

        pasangAksi();
    }

    // ------------------------------------------------------------- tampilan

    private JPanel buatFilter() {
        JPanel kartu = Tema.kartu();
        kartu.setLayout(new BorderLayout(10, 0));

        JPanel kiri = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        kiri.setOpaque(false);
        JLabel labelCari = Tema.teks("Cari");
        labelCari.setForeground(Tema.TEXT_SEKUNDER);
        kiri.add(labelCari);
        kiri.add(txtCari);
        kiri.add(cbStatus);
        kiri.add(cbJenis);
        kiri.add(btnCari);
        kiri.add(btnReset);
        kartu.add(kiri, BorderLayout.CENTER);
        kartu.add(btnUnduh, BorderLayout.EAST);
        return kartu;
    }

    private JPanel buatTabel() {
        JPanel kartu = Tema.kartu();
        kartu.setLayout(new BorderLayout(0, 10));

        int[] lebar = { 160, 70, 70, 140, 140, 65, 100, 100, 120 };
        for (int i = 0; i < lebar.length; i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());

        JScrollPane scroll = Tema.gulir(tabel);
        kartu.add(scroll, BorderLayout.CENTER);
        kartu.add(lblInfo, BorderLayout.SOUTH);
        return kartu;
    }

    /** Pewarnaan teks status agar mudah dipindai. */
    private static final class StatusRenderer extends Tema.RenderBaris {

        private static final long serialVersionUID = 1L;

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable tabel, Object nilai, boolean terpilih,
                boolean fokus, int baris, int kolom) {
            java.awt.Component komponen = super.getTableCellRendererComponent(tabel, nilai, terpilih, fokus,
                    baris, kolom);
            String teks = nilai == null ? "" : nilai.toString();
            if ("Selesai".equals(teks)) {
                komponen.setForeground(Tema.SUKSES);
            } else if ("Aktif".equals(teks)) {
                komponen.setForeground(Tema.CYAN);
            } else if ("Dibatalkan".equals(teks)) {
                komponen.setForeground(Tema.PERINGATAN);
            }
            return komponen;
        }
    }

    // ---------------------------------------------------------------- aksi

    private void pasangAksi() {
        btnCari.addActionListener(e -> cari());
        btnReset.addActionListener(e -> {
            txtCari.setText("");
            cbStatus.setSelectedIndex(0);
            cbJenis.setSelectedIndex(0);
            cari();
        });
        txtCari.addActionListener(e -> cari());
        btnUnduh.addActionListener(e -> unduhBukti());
        tabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    tampilkanDetail();
                }
            }
        });
    }

    @Override
    public void muatUlang() {
        cari();
    }

    private void cari() {
        StatusTiket status = cbStatus.getSelectedItem() instanceof StatusTiket
                ? (StatusTiket) cbStatus.getSelectedItem() : null;
        JenisKendaraan jenis = cbJenis.getSelectedItem() instanceof JenisKendaraan
                ? (JenisKendaraan) cbJenis.getSelectedItem() : null;

        try {
            data = frame.parkir().cariRiwayat(txtCari.getText(), status, jenis);
        } catch (DataAccessException e) {
            data = new ArrayList<>();
            galat("Gagal membaca riwayat: " + e.getMessage());
        }

        List<Object[]> baris = new ArrayList<>();
        for (Tiket tiket : data) {
            baris.add(new Object[] {
                    tiket.getNomorTiket(),
                    tiket.getSlot().getNomor(),
                    tiket.getJenis().getLabel(),
                    Tema.waktu(tiket.getWaktuMasuk()),
                    Tema.waktu(tiket.getWaktuKeluar()),
                    tiket.getDurasiJam() > 0 ? tiket.getDurasiJam() + " jam" : "—",
                    tiket.getStatus() == StatusTiket.SELESAI ? Money.format(tiket.getTotal()) : "—",
                    tiket.getStatus().getLabel(),
                    tiket.getPetugasMasukNama() == null ? "—" : tiket.getPetugasMasukNama()
            });
        }
        Tema.isiTabel(tabel, baris);
        lblInfo.setText("Menampilkan " + data.size() + " transaksi. Klik dua kali untuk melihat detail.");
        btnUnduh.setEnabled(!data.isEmpty());
    }

    private void tampilkanDetail() {
        int baris = Tema.barisTerpilih(tabel);
        if (baris < 0) {
            return;
        }
        Tiket tiket = data.get(baris);
        JTextArea area = Tema.areaDialog("Nomor tiket   : " + tiket.getNomorTiket() + "\n"
                + "Slot          : " + tiket.getSlot().getNomor() + "\n"
                + "Kendaraan     : " + tiket.getJenis().getLabel() + "\n"
                + "Masuk         : " + Tema.waktu(tiket.getWaktuMasuk()) + "\n"
                + "Keluar        : " + Tema.waktu(tiket.getWaktuKeluar()) + "\n"
                + "Durasi        : " + (tiket.getDurasiJam() > 0 ? tiket.getDurasiJam() + " jam" : "—") + "\n\n"
                + "Tarif dasar   : " + Money.format(tiket.getTarifJamPertamaSnapshot()) + " (snapshot)\n"
                + "Denda overstay: " + Money.format(tiket.getDendaOverstay()) + "\n"
                + "Denda hilang  : " + Money.format(tiket.getDendaTiketHilang()) + "\n"
                + "Total         : " + Money.format(tiket.getTotal()) + "\n"
                + "Status        : " + tiket.getStatus().getLabel() + "\n"
                + "Petugas masuk : " + text(tiket.getPetugasMasukNama()) + "\n"
                + "Petugas selesai: " + text(tiket.getPetugasSelesaiNama()) + "\n"
                + (tiket.getAlasanPembatalan() == null ? ""
                        : "Alasan batal  : " + tiket.getAlasanPembatalan() + "\n"));

        JOptionPane.showMessageDialog(this, area, "Detail Transaksi", JOptionPane.INFORMATION_MESSAGE);
    }

    private String text(String nilai) {
        return nilai == null || nilai.isBlank() ? "—" : nilai;
    }

    private void unduhBukti() {
        int baris = Tema.barisTerpilih(tabel);
        if (baris < 0) {
            galat("Pilih satu transaksi pada tabel terlebih dahulu.");
            return;
        }
        Tiket tiket = data.get(baris);

        JFileChooser chooser = new JFileChooser();
        Tema.gayaPemilihBerkas(chooser);
        chooser.setDialogTitle("Simpan bukti transaksi");
        chooser.setSelectedFile(new File("bukti-" + tiket.getNomorTiket() + ".txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path tujuan = chooser.getSelectedFile().toPath();
        if (!tujuan.getFileName().toString().toLowerCase().endsWith(".txt")) {
            tujuan = Path.of(tujuan.toString() + ".txt");
        }
        try {
            frame.parkir().unduhBukti(tiket, tujuan);
            JOptionPane.showMessageDialog(this, "Bukti transaksi disimpan di:\n" + tujuan.toAbsolutePath(),
                    "Unduh Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidasiException e) {
            galat(e.getMessage());
        } catch (DataAccessException e) {
            galat("Gagal menulis berkas: " + e.getMessage());
        }
    }

    private void galat(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Tidak dapat diproses", JOptionPane.WARNING_MESSAGE);
    }
}
