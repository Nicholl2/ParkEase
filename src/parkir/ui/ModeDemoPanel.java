package parkir.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import parkir.util.Jam;

/**
 * Mode demo: memajukan jam aplikasi agar overstay bisa diperagakan tanpa
 * menunggu delapan jam. Waktu tetap diambil dari {@link Jam}.
 */
public class ModeDemoPanel extends JPanel implements MuatUlang {

    private static final long serialVersionUID = 1L;

    private final MainFrame frame;

    private final JLabel lblSekarang = Tema.angka("");
    private final JLabel lblSistem = Tema.kecil("");
    private final JSpinner spOffset = new JSpinner(new SpinnerNumberModel(0, 0, 48, 1));
    private final JButton btnTerapkan = Tema.utama("Terapkan");
    private final JButton btnReset = Tema.kedua("Kembali ke Waktu Asli");

    public ModeDemoPanel(MainFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout(18, 0));

        Tema.gayaSpinner(spOffset);
        lblSekarang.setForeground(Tema.CYAN);

        add(buatPanelUtama(), BorderLayout.CENTER);
        add(buatPanelPanduan(), BorderLayout.EAST);
        pasangAksi();
    }

    // ------------------------------------------------------------- tampilan

    private JPanel buatPanelUtama() {
        JPanel kartu = Tema.kartu();
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));

        JLabel judul = Tema.subjudul("Pengaturan waktu aplikasi");
        judul.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(judul);
        kartu.add(Box.createVerticalStrut(4));

        JTextArea keterangan = Tema.catatan("Seluruh perhitungan durasi memakai sumber waktu ini, "
                + "bukan jam sistem secara langsung.", 400);
        kartu.add(keterangan);
        kartu.add(Box.createVerticalStrut(24));

        JLabel labelSekarang = Tema.kecil("WAKTU APLIKASI SEKARANG");
        labelSekarang.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(labelSekarang);
        lblSekarang.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(lblSekarang);
        kartu.add(Box.createVerticalStrut(6));

        lblSistem.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(lblSistem);
        kartu.add(Box.createVerticalStrut(26));

        JPanel spinner = Tema.berlabel("MAJUKAN WAKTU (JAM)", spOffset);
        spinner.setAlignmentX(LEFT_ALIGNMENT);
        spinner.setMaximumSize(new Dimension(260, 70));
        kartu.add(spinner);
        kartu.add(Box.createVerticalStrut(12));

        JPanel tombol = Tema.baris(btnTerapkan, btnReset);
        tombol.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(tombol);
        kartu.add(Box.createVerticalStrut(20));

        JTextArea catatan = Tema.catatan("Contoh: catat kendaraan masuk pada waktu asli, majukan 9 jam, "
                + "lalu proses keluar. Denda overstay akan muncul otomatis. Kembalikan offset ke 0 "
                + "setelah demo selesai.", 400);
        kartu.add(catatan);
        kartu.add(Box.createVerticalGlue());
        return kartu;
    }

    private JPanel buatPanelPanduan() {
        JPanel kartu = Tema.kartu();
        kartu.setPreferredSize(new Dimension(330, 0));
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));

        JLabel judul = Tema.subjudul("Skenario demo overstay");
        judul.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(judul);
        kartu.add(Box.createVerticalStrut(14));

        String[] langkah = {
                "Catat kendaraan masuk di menu Kendaraan Masuk.",
                "Buka halaman ini dan majukan waktu 9 jam.",
                "Buka Kendaraan Keluar, cari nomor tiket tadi.",
                "Denda overstay muncul pada rincian biaya.",
                "Proses pembayaran, lalu kembalikan waktu ke 0."
        };
        for (int i = 0; i < langkah.length; i++) {
            kartu.add(Tema.paragraf((i + 1) + ". " + langkah[i], 270));
            kartu.add(Box.createVerticalStrut(10));
        }

        kartu.add(Box.createVerticalStrut(10));
        kartu.add(Tema.catatan("Overstay dihitung bila durasi melebihi batas jam reguler pada tarif.", 270));
        kartu.add(Box.createVerticalGlue());
        return kartu;
    }

    // ---------------------------------------------------------------- aksi

    private void pasangAksi() {
        btnTerapkan.addActionListener(e -> terapkan());
        btnReset.addActionListener(e -> reset());
    }

    @Override
    public void muatUlang() {
        spOffset.setValue(Jam.getOffsetJam());
        segarkanWaktu();
    }

    private void segarkanWaktu() {
        lblSekarang.setText(Tema.waktu(Jam.sekarang()));
        lblSistem.setText("<html><div style='width:420px'>Waktu sistem saat ini: "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
                + " · offset " + Jam.getOffsetJam() + " jam</div></html>");
        frame.segarkanWaktu();
    }

    private void terapkan() {
        int jam = ((Number) spOffset.getValue()).intValue();
        Jam.setOffsetJam(jam);
        segarkanWaktu();
        JOptionPane.showMessageDialog(this,
                "Waktu aplikasi dimajukan " + jam + " jam.\nSekarang: " + Tema.waktu(Jam.sekarang()),
                "Waktu Diperbarui", JOptionPane.INFORMATION_MESSAGE);
    }

    private void reset() {
        Jam.setOffsetJam(0);
        spOffset.setValue(0);
        segarkanWaktu();
        JOptionPane.showMessageDialog(this, "Waktu aplikasi kembali ke jam sistem.",
                "Waktu Direset", JOptionPane.INFORMATION_MESSAGE);
    }
}
