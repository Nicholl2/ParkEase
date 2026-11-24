package parkir.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import parkir.exception.DataAccessException;
import parkir.exception.ValidasiException;
import parkir.model.MetodePembayaran;
import parkir.model.Pembayaran;
import parkir.model.RincianBiaya;
import parkir.model.Tiket;
import parkir.util.Money;

/** Layar kendaraan keluar: cari tiket, hitung biaya, bayar, atau batalkan. */
public class KeluarPanel extends JPanel implements MuatUlang {

    private static final long serialVersionUID = 1L;

    private final MainFrame frame;

    private final javax.swing.JTextField txtNomor = Tema.field(16);
    private final JButton btnCari = Tema.kedua("Cari");
    private final JCheckBox cbHilang = new JCheckBox("Tiket hilang (dikenakan denda)");
    private final JComboBox<MetodePembayaran> cbMetode = new JComboBox<>(MetodePembayaran.semua());
    private final JButton btnBayar = Tema.utama("Proses Pembayaran");
    private final JButton btnBatal = Tema.bahaya("Batalkan Tiket");

    private final JLabel lblSlot = nilai("-");
    private final JLabel lblJenis = nilai("-");
    private final JLabel lblMasuk = nilai("-");
    private final JLabel lblDurasi = nilai("-");
    private final JLabel lblTarifDasar = nilai("-");
    private final JLabel lblDendaOverstay = nilai("-");
    private final JLabel lblDendaHilang = nilai("-");
    private final JLabel lblTotal = Tema.angka("Rp0");
    private final JLabel lblTotalBayar = Tema.angka("Rp0");

    private Tiket aktif;

    public KeluarPanel(MainFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout(18, 0));

        Tema.gayaCek(cbHilang);
        lblTotal.setForeground(Tema.SUKSES);
        lblTotalBayar.setForeground(Tema.SUKSES);

        add(buatPanelKiri(), BorderLayout.CENTER);
        add(buatPanelBayar(), BorderLayout.EAST);
        Tema.gayaCombo(cbMetode);
        pasangAksi();
        bersihkan();
    }

    private static JLabel nilai(String teks) {
        JLabel label = new JLabel(teks);
        label.setFont(Tema.FONT_TEBAL);
        label.setForeground(Tema.TEXT);
        return label;
    }

    // ------------------------------------------------------------- tampilan

    private JPanel buatPanelKiri() {
        JPanel kartu = Tema.kartu();
        kartu.setLayout(new BorderLayout(0, 16));

        JPanel atas = new JPanel();
        atas.setOpaque(false);
        atas.setLayout(new BoxLayout(atas, BoxLayout.Y_AXIS));

        JLabel labelNomor = Tema.labelKapital("Nomor tiket");
        labelNomor.setAlignmentX(LEFT_ALIGNMENT);
        atas.add(labelNomor);
        atas.add(Box.createVerticalStrut(5));

        JPanel baris = new JPanel(new BorderLayout(8, 0));
        baris.setOpaque(false);
        baris.setAlignmentX(LEFT_ALIGNMENT);
        baris.add(txtNomor, BorderLayout.CENTER);
        baris.add(btnCari, BorderLayout.EAST);
        atas.add(baris);
        atas.add(Box.createVerticalStrut(6));

        JLabel petunjuk = Tema.kecil("Contoh: PKR-20260105-AB2C. Tekan Enter untuk mencari.");
        petunjuk.setAlignmentX(LEFT_ALIGNMENT);
        atas.add(petunjuk);

        kartu.add(atas, BorderLayout.NORTH);
        kartu.add(buatDetail(), BorderLayout.CENTER);

        JLabel catatan = Tema.kecil("Denda overstay muncul bila durasi melebihi batas reguler tarif.");
        kartu.add(catatan, BorderLayout.SOUTH);
        return kartu;
    }

    private JPanel buatDetail() {
        JPanel detail = new JPanel(new GridBagLayout());
        detail.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        tambahDetail(detail, gbc, 0, "Slot", lblSlot);
        tambahDetail(detail, gbc, 1, "Jenis kendaraan", lblJenis);
        tambahDetail(detail, gbc, 2, "Waktu masuk", lblMasuk);
        tambahDetail(detail, gbc, 3, "Durasi", lblDurasi);
        tambahDetail(detail, gbc, 4, "Tarif dasar", lblTarifDasar);
        tambahDetail(detail, gbc, 5, "Denda overstay", lblDendaOverstay);
        tambahDetail(detail, gbc, 6, "Denda tiket hilang", lblDendaHilang);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(14, 0, 0, 18);
        detail.add(Tema.kecil("TOTAL"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 0, 0);
        detail.add(lblTotal, gbc);
        return detail;
    }

    private void tambahDetail(JPanel panel, GridBagConstraints gbc, int baris, String label, JLabel nilai) {
        gbc.gridy = baris;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 8, 18);
        JLabel kunci = Tema.teks(label);
        kunci.setForeground(Tema.TEXT_SEKUNDER);
        panel.add(kunci, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(nilai, gbc);
    }

    private JPanel buatPanelBayar() {
        JPanel kartu = Tema.kartu();
        kartu.setPreferredSize(new Dimension(320, 0));
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));

        JLabel judul = Tema.subjudul("Pembayaran");
        judul.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(judul);
        kartu.add(Box.createVerticalStrut(12));

        cbHilang.setOpaque(false);
        cbHilang.setAlignmentX(LEFT_ALIGNMENT);
        cbHilang.setFont(Tema.FONT_BADAN);
        kartu.add(cbHilang);
        kartu.add(Box.createVerticalStrut(12));

        cbMetode.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean terpilih, boolean fokus) {
                super.getListCellRendererComponent(list, value, index, terpilih, fokus);
                if (value instanceof MetodePembayaran) {
                    setText(((MetodePembayaran) value).getLabel());
                }
                return this;
            }
        });
        JPanel metode = Tema.berlabel("METODE PEMBAYARAN", cbMetode);
        kartu.add(metode);
        kartu.add(Box.createVerticalStrut(18));

        JLabel labelTotal = Tema.labelKapital("Jumlah dibayar");
        labelTotal.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(labelTotal);
        lblTotalBayar.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(lblTotalBayar);
        kartu.add(Box.createVerticalStrut(18));

        btnBayar.setAlignmentX(LEFT_ALIGNMENT);
        btnBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        kartu.add(btnBayar);
        kartu.add(Box.createVerticalStrut(22));

        JLabel pemisah = Tema.labelKapital("Tindakan lain");
        pemisah.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(pemisah);
        kartu.add(Box.createVerticalStrut(8));

        btnBatal.setAlignmentX(LEFT_ALIGNMENT);
        btnBatal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        kartu.add(btnBatal);
        kartu.add(Box.createVerticalStrut(8));

        JTextArea catatan = Tema.catatan("Pembatalan tidak menghapus riwayat; tiket tetap tersimpan "
                + "dengan status Dibatalkan.", 260);
        kartu.add(catatan);
        kartu.add(Box.createVerticalGlue());
        return kartu;
    }

    // ---------------------------------------------------------------- aksi

    private void pasangAksi() {
        btnCari.addActionListener(e -> cari());
        txtNomor.addActionListener(e -> cari());
        cbHilang.addActionListener(e -> hitung());
        btnBayar.addActionListener(e -> bayar());
        btnBatal.addActionListener(e -> batalkan());
    }

    @Override
    public void muatUlang() {
        bersihkan();
    }

    private void cari() {
        String nomor = txtNomor.getText().trim().toUpperCase();
        if (nomor.isEmpty()) {
            galat("Masukkan nomor tiket terlebih dahulu.");
            return;
        }
        Tiket tiket = frame.parkir().cariAktif(nomor);
        if (tiket == null) {
            aktif = null;
            bersihkanDetail();
            galat("Tiket " + nomor + " tidak ditemukan atau sudah selesai.");
            return;
        }
        aktif = tiket;
        isiDetail(tiket);
        btnBayar.setEnabled(true);
        btnBatal.setEnabled(true);
    }

    private void isiDetail(Tiket tiket) {
        lblSlot.setText(tiket.getSlot().getNomor());
        lblJenis.setText(tiket.getJenis().getLabel());
        lblMasuk.setText(Tema.waktu(tiket.getWaktuMasuk()));
        hitung();
    }

    private void hitung() {
        if (aktif == null) {
            return;
        }
        try {
            RincianBiaya rincian = frame.parkir().hitungTotal(aktif.getNomorTiket(), cbHilang.isSelected());
            lblDurasi.setText(rincian.getDurasiJam() + " jam");
            lblTarifDasar.setText(Money.format(rincian.getTarifDasar()));
            lblDendaOverstay.setText(Money.format(rincian.getDendaOverstay()));
            lblDendaHilang.setText(Money.format(rincian.getDendaTiketHilang()));
            lblTotal.setText(Money.format(rincian.getTotal()));
            lblTotalBayar.setText(Money.format(rincian.getTotal()));
        } catch (ValidasiException e) {
            galat(e.getMessage());
        }
    }

    private void bayar() {
        if (aktif == null) {
            galat("Cari tiket terlebih dahulu.");
            return;
        }
        MetodePembayaran metode = (MetodePembayaran) cbMetode.getSelectedItem();
        if (metode == null) {
            galat("Pilih metode pembayaran.");
            return;
        }
        try {
            RincianBiaya rincian = frame.parkir().hitungTotal(aktif.getNomorTiket(), cbHilang.isSelected());
            int pilih = JOptionPane.showConfirmDialog(this,
                    "Proses pembayaran " + Money.format(rincian.getTotal())
                            + " via " + metode.getLabel() + "?",
                    "Konfirmasi Pembayaran", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (pilih != JOptionPane.YES_OPTION) {
                return;
            }

            Pembayaran bayar = cbHilang.isSelected()
                    ? frame.parkir().tiketHilang(aktif.getNomorTiket(), metode, frame.getPetugas())
                    : frame.parkir().keluar(aktif.getNomorTiket(), metode, frame.getPetugas());

            tampilkanStruk(bayar, rincian, metode);
            bersihkan();
        } catch (ValidasiException e) {
            galat(e.getMessage());
        } catch (DataAccessException e) {
            galat("Gagal memproses pembayaran: " + e.getMessage());
        }
    }

    private void tampilkanStruk(Pembayaran bayar, RincianBiaya rincian, MetodePembayaran metode) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(Tema.FONT_MONO);
        area.setText("Pembayaran berhasil.\n\n"
                + "Nomor tiket : " + (aktif == null ? "-" : aktif.getNomorTiket()) + "\n"
                + "Durasi      : " + rincian.getDurasiJam() + " jam\n"
                + "Tarif dasar : " + Money.format(rincian.getTarifDasar()) + "\n"
                + "Denda       : " + Money.format(rincian.getDendaOverstay() + rincian.getDendaTiketHilang()) + "\n"
                + "Total       : " + Money.format(bayar.getJumlah()) + "\n"
                + "Metode      : " + metode.getLabel() + "\n\n"
                + "Slot sudah dikosongkan.");
        JOptionPane.showMessageDialog(this, area, "Bukti Pembayaran", JOptionPane.INFORMATION_MESSAGE);
    }

    private void batalkan() {
        if (aktif == null) {
            galat("Cari tiket terlebih dahulu.");
            return;
        }
        String alasan = JOptionPane.showInputDialog(this,
                "Alasan pembatalan tiket " + aktif.getNomorTiket() + ":",
                "Batalkan Tiket", JOptionPane.WARNING_MESSAGE);
        if (alasan == null) {
            return;
        }
        if (alasan.isBlank()) {
            galat("Alasan pembatalan wajib diisi.");
            return;
        }
        int pilih = JOptionPane.showConfirmDialog(this,
                "Batalkan tiket " + aktif.getNomorTiket() + "?\nAlasan: " + alasan.trim(),
                "Konfirmasi Pembatalan", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (pilih != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String nomor = aktif.getNomorTiket();
            frame.parkir().batalkan(nomor, alasan, frame.getPetugas());
            JOptionPane.showMessageDialog(this,
                    "Tiket " + nomor + " dibatalkan dan slotnya sudah dikosongkan.",
                    "Tiket Dibatalkan", JOptionPane.INFORMATION_MESSAGE);
            bersihkan();
        } catch (ValidasiException e) {
            galat(e.getMessage());
        } catch (DataAccessException e) {
            galat("Gagal membatalkan tiket: " + e.getMessage());
        }
    }

    private void bersihkan() {
        aktif = null;
        txtNomor.setText("");
        cbHilang.setSelected(false);
        btnBayar.setEnabled(false);
        btnBatal.setEnabled(false);
        bersihkanDetail();
        txtNomor.requestFocusInWindow();
    }

    private void bersihkanDetail() {
        lblSlot.setText("-");
        lblJenis.setText("-");
        lblMasuk.setText("-");
        lblDurasi.setText("-");
        lblTarifDasar.setText("-");
        lblDendaOverstay.setText("-");
        lblDendaHilang.setText("-");
        lblTotal.setText("Rp0");
        lblTotalBayar.setText("Rp0");
    }

    private void galat(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Tidak dapat diproses", JOptionPane.WARNING_MESSAGE);
    }
}
