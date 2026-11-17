package parkir.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import parkir.exception.DataAccessException;
import parkir.exception.ValidasiException;
import parkir.model.JenisKendaraan;
import parkir.model.PengaturanTarif;
import parkir.util.Money;

/** Pengaturan tarif dan denda per jenis kendaraan. */
public class TarifPanel extends JPanel implements MuatUlang {

    private static final long serialVersionUID = 1L;

    private final MainFrame frame;

    private final JComboBox<JenisKendaraan> cbJenis = new JComboBox<>(JenisKendaraan.values());
    private final JLabel lblBerlaku = Tema.kecil("");

    private final JTextField txtPertama = Tema.field(12);
    private final JTextField txtBerikutnya = Tema.field(12);
    private final JTextField txtDendaOverstay = Tema.field(12);
    private final JTextField txtDendaHilang = Tema.field(12);
    private final JSpinner spBatas = new JSpinner(new SpinnerNumberModel(8, 1, 24, 1));
    private final JButton btnSimpan = Tema.utama("Simpan Tarif");

    public TarifPanel(MainFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        add(buatPemilih(), BorderLayout.NORTH);
        add(buatFormulir(), BorderLayout.CENTER);
        pasangAksi();
    }

    // ------------------------------------------------------------- tampilan

    private JPanel buatPemilih() {
        JPanel kartu = Tema.kartu();
        kartu.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));

        cbJenis.setPreferredSize(new Dimension(160, 34));
        kartu.add(Tema.teks("Jenis kendaraan"));
        kartu.add(cbJenis);
        kartu.add(lblBerlaku);
        return kartu;
    }

    private JPanel buatFormulir() {
        JPanel kartu = Tema.kartu();
        kartu.setLayout(new BorderLayout(24, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(440, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);

        form.add(Tema.berlabel("TARIF JAM PERTAMA (RUPIAH)", txtPertama), gbc);
        gbc.gridy++;
        form.add(Tema.berlabel("TARIF JAM BERIKUTNYA (RUPIAH)", txtBerikutnya), gbc);
        gbc.gridy++;
        form.add(Tema.berlabel("BATAS JAM REGULER", spBatas), gbc);
        gbc.gridy++;
        form.add(Tema.berlabel("DENDA OVERSTAY (RUPIAH)", txtDendaOverstay), gbc);
        gbc.gridy++;
        form.add(Tema.berlabel("DENDA TIKET HILANG (RUPIAH)", txtDendaHilang), gbc);
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 0, 0);
        form.add(btnSimpan, gbc);

        kartu.add(form, BorderLayout.WEST);
        kartu.add(buatPenjelasan(), BorderLayout.CENTER);
        return kartu;
    }

    private JPanel buatPenjelasan() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel judul = Tema.subjudul("Cara perhitungan");
        judul.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(judul);
        panel.add(Box.createVerticalStrut(10));

        JLabel teks = new JLabel("<html><div style='width:300px'>"
                + "Durasi dibulatkan ke atas per jam dan minimal satu jam.<br><br>"
                + "Setelah melewati batas jam reguler, tarif dasar berhenti bertambah "
                + "dan denda overstay dikenakan satu kali.<br><br>"
                + "Tiket hilang menambah denda tiket hilang.<br><br>"
                + "<b>Perubahan tarif hanya berlaku untuk tiket baru.</b> "
                + "Tiket yang masih aktif memakai tarif saat kendaraan masuk."
                + "</div></html>");
        teks.setFont(Tema.FONT_BADAN);
        teks.setForeground(Tema.MUTED);
        teks.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(teks);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    // ---------------------------------------------------------------- aksi

    private void pasangAksi() {
        cbJenis.addActionListener(e -> isiForm());
        btnSimpan.addActionListener(e -> simpan());
    }

    @Override
    public void muatUlang() {
        isiForm();
    }

    private void isiForm() {
        JenisKendaraan jenis = jenisTerpilih();
        if (jenis == null) {
            return;
        }
        PengaturanTarif tarif = frame.tarif().ambil(jenis);
        if (tarif == null) {
            txtPertama.setText("");
            txtBerikutnya.setText("");
            txtDendaOverstay.setText("");
            txtDendaHilang.setText("");
            spBatas.setValue(8);
            lblBerlaku.setText("Belum diatur");
            return;
        }
        txtPertama.setText(String.valueOf(tarif.getTarifJamPertama()));
        txtBerikutnya.setText(String.valueOf(tarif.getTarifJamBerikutnya()));
        txtDendaOverstay.setText(String.valueOf(tarif.getDendaOverstay()));
        txtDendaHilang.setText(String.valueOf(tarif.getDendaTiketHilang()));
        spBatas.setValue(tarif.getBatasJamReguler());
        lblBerlaku.setText("Berlaku sejak " + Tema.waktu(tarif.getBerlakuSejak())
                + " · " + Money.format(tarif.getTarifJamPertama()) + "/jam pertama");
    }

    private void simpan() {
        JenisKendaraan jenis = jenisTerpilih();
        if (jenis == null) {
            galat("Pilih jenis kendaraan terlebih dahulu.");
            return;
        }
        try {
            PengaturanTarif tarif = new PengaturanTarif(
                    jenis,
                    Tema.bacaUang(txtPertama.getText()),
                    Tema.bacaUang(txtBerikutnya.getText()),
                    ((Number) spBatas.getValue()).intValue(),
                    Tema.bacaUang(txtDendaOverstay.getText()),
                    Tema.bacaUang(txtDendaHilang.getText()));
            frame.tarif().simpan(tarif, frame.getPetugas());
            info("Tarif " + jenis.getLabel().toLowerCase()
                    + " disimpan. Perubahan berlaku untuk tiket baru.");
            isiForm();
        } catch (ValidasiException e) {
            galat(e.getMessage());
        } catch (DataAccessException e) {
            galat("Gagal menyimpan tarif: " + e.getMessage());
        }
    }

    private JenisKendaraan jenisTerpilih() {
        Object nilai = cbJenis.getSelectedItem();
        return nilai instanceof JenisKendaraan ? (JenisKendaraan) nilai : null;
    }

    private void info(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Berhasil", JOptionPane.INFORMATION_MESSAGE);
    }

    private void galat(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Tidak dapat diproses", JOptionPane.WARNING_MESSAGE);
    }
}
