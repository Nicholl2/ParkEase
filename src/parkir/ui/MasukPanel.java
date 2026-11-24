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
import javax.swing.border.EmptyBorder;

import parkir.exception.DataAccessException;
import parkir.exception.ValidasiException;
import parkir.model.JenisKendaraan;
import parkir.model.PengaturanTarif;
import parkir.model.RincianBiaya;
import parkir.model.SlotParkir;
import parkir.model.Tiket;
import parkir.util.Money;

/** Layar pencatatan kendaraan masuk. */
public class MasukPanel extends JPanel implements MuatUlang {

    private static final long serialVersionUID = 1L;

    private final MainFrame frame;

    private final JComboBox<JenisKendaraan> cbJenis = new JComboBox<>(JenisKendaraan.values());
    private final JComboBox<SlotParkir> cbSlot = new JComboBox<>();
    private final JCheckBox cbOtomatis = new JCheckBox("Pilih slot kosong secara otomatis");
    private final JLabel lblInfoSlot = Tema.kecil("");
    private final JLabel lblTarif = new JLabel();
    private final JButton btnMasuk = Tema.utama("Catat Kendaraan Masuk");

    public MasukPanel(MainFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout(18, 0));

        Tema.gayaCek(cbOtomatis);

        add(buatFormulir(), BorderLayout.CENTER);
        add(buatPanelTarif(), BorderLayout.EAST);
        Tema.gayaCombo(cbJenis);
        Tema.gayaCombo(cbSlot);
        pasangAksi();
    }

    // ------------------------------------------------------------- tampilan

    private JPanel buatFormulir() {
        JPanel kartu = Tema.kartu();
        kartu.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 14, 0);

        cbSlot.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean terpilih, boolean fokus) {
                super.getListCellRendererComponent(list, value, index, terpilih, fokus);
                if (value instanceof SlotParkir) {
                    setText(((SlotParkir) value).getNomor());
                }
                return this;
            }
        });

        kartu.add(Tema.berlabel("JENIS KENDARAAN", cbJenis), gbc);
        gbc.gridy++;
        kartu.add(cbOtomatis, gbc);
        gbc.gridy++;
        kartu.add(Tema.berlabel("SLOT PARKIR", cbSlot), gbc);
        gbc.gridy++;
        lblInfoSlot.setBorder(new EmptyBorder(0, 0, 12, 0));
        kartu.add(lblInfoSlot, gbc);
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        kartu.add(btnMasuk, gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(16, 0, 0, 0);
        kartu.add(Box.createVerticalGlue(), gbc);
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        JLabel catatan = Tema.kecil("Nomor tiket dibuat otomatis dan wajib diserahkan ke pengendara.");
        kartu.add(catatan, gbc);
        return kartu;
    }

    private JPanel buatPanelTarif() {
        JPanel kartu = Tema.kartu();
        kartu.setPreferredSize(new Dimension(320, 0));
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));

        JLabel judul = Tema.subjudul("Tarif berlaku");
        judul.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(judul);
        kartu.add(Box.createVerticalStrut(10));

        lblTarif.setFont(Tema.FONT_BADAN);
        lblTarif.setForeground(Tema.TEXT);
        lblTarif.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(lblTarif);
        kartu.add(Box.createVerticalStrut(14));

        JTextArea catatan = Tema.catatan("Tarif dikunci saat kendaraan masuk, sehingga perubahan tarif "
                + "hanya berlaku untuk tiket berikutnya.", 260);
        kartu.add(catatan);
        kartu.add(Box.createVerticalGlue());
        return kartu;
    }

    // ---------------------------------------------------------------- aksi

    private void pasangAksi() {
        cbJenis.addActionListener(e -> {
            muatSlot();
            muatTarif();
        });
        cbOtomatis.addActionListener(e -> perbaruiSlot());
        btnMasuk.addActionListener(e -> catat());
    }

    @Override
    public void muatUlang() {
        muatSlot();
        muatTarif();
    }

    private void muatSlot() {
        JenisKendaraan jenis = (JenisKendaraan) cbJenis.getSelectedItem();
        cbSlot.removeAllItems();
        if (jenis == null) {
            return;
        }
        for (SlotParkir slot : frame.slot().kosong(jenis)) {
            cbSlot.addItem(slot);
        }
        perbaruiSlot();
    }

    private void perbaruiSlot() {
        boolean otomatis = cbOtomatis.isSelected();
        cbSlot.setEnabled(!otomatis && cbSlot.getItemCount() > 0);
        if (cbSlot.getItemCount() == 0) {
            lblInfoSlot.setText("Tidak ada slot kosong untuk jenis ini.");
        } else if (otomatis) {
            lblInfoSlot.setText(cbSlot.getItemCount() + " slot kosong — sistem memilih otomatis.");
        } else {
            lblInfoSlot.setText(cbSlot.getItemCount() + " slot kosong tersedia.");
        }
    }

    private void muatTarif() {
        JenisKendaraan jenis = (JenisKendaraan) cbJenis.getSelectedItem();
        if (jenis == null) {
            return;
        }
        PengaturanTarif tarif = frame.tarif().ambil(jenis);
        if (tarif == null) {
            lblTarif.setText("<html>Tarif belum diatur.</html>");
            return;
        }
        lblTarif.setText("<html>"
                + "<b>" + jenis.getLabel() + "</b><br><br>"
                + "Jam pertama<br><b>" + Money.format(tarif.getTarifJamPertama()) + "</b><br><br>"
                + "Jam berikutnya<br><b>" + Money.format(tarif.getTarifJamBerikutnya()) + "</b><br><br>"
                + "Batas reguler<br><b>" + tarif.getBatasJamReguler() + " jam</b><br><br>"
                + "Denda overstay<br><b>" + Money.format(tarif.getDendaOverstay()) + "</b><br><br>"
                + "Denda tiket hilang<br><b>" + Money.format(tarif.getDendaTiketHilang()) + "</b>"
                + "</html>");
    }

    private void catat() {
        JenisKendaraan jenis = (JenisKendaraan) cbJenis.getSelectedItem();
        SlotParkir slot = cbOtomatis.isSelected() ? null : (SlotParkir) cbSlot.getSelectedItem();
        if (jenis == null) {
            galat("Pilih jenis kendaraan terlebih dahulu.");
            return;
        }
        if (!cbOtomatis.isSelected() && slot == null) {
            galat("Tidak ada slot yang bisa dipilih. Centang pilihan otomatis atau kosongkan slot lain.");
            return;
        }

        try {
            Tiket tiket = frame.parkir().masuk(jenis, slot == null ? null : slot.getNomor(), frame.getPetugas());
            tampilkanTiket(tiket);
            muatUlang();
        } catch (ValidasiException e) {
            galat(e.getMessage());
        } catch (DataAccessException e) {
            galat("Gagal menyimpan data: " + e.getMessage());
        }
    }

    private void tampilkanTiket(Tiket tiket) {
        RincianBiaya rincian = tiket.hitungRincian();
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(Tema.FONT_MONO);
        area.setText("Kendaraan berhasil dicatat.\n\n"
                + "Nomor tiket : " + tiket.getNomorTiket() + "\n"
                + "Slot        : " + tiket.getSlot().getNomor() + "\n"
                + "Kendaraan   : " + tiket.getJenis().getLabel() + "\n"
                + "Masuk       : " + Tema.waktu(tiket.getWaktuMasuk()) + "\n"
                + "Estimasi    : " + Money.format(rincian.getTotal()) + " ("
                + rincian.getDurasiJam() + " jam)\n\n"
                + "Serahkan nomor tiket ini kepada pengendara.");

        JOptionPane.showMessageDialog(this, area, "Tiket Parkir", JOptionPane.INFORMATION_MESSAGE);
    }

    private void galat(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Tidak dapat diproses", JOptionPane.WARNING_MESSAGE);
    }
}
