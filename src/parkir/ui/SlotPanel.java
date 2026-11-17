package parkir.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import parkir.exception.DataAccessException;
import parkir.exception.ValidasiException;
import parkir.model.JenisKendaraan;
import parkir.model.SlotParkir;

/** CRUD Slot Parkir: tabel di kiri, formulir di kanan. */
public class SlotPanel extends JPanel implements MuatUlang {

    private static final long serialVersionUID = 1L;

    private final MainFrame frame;

    private final JTextField txtCari = Tema.field(16);
    private final JTable tabel = Tema.tabel("Nomor", "Jenis", "Status");
    private final JLabel lblInfo = Tema.kecil("");

    private final JTextField txtNomor = Tema.field(14);
    private final JComboBox<JenisKendaraan> cbJenis = new JComboBox<>(JenisKendaraan.values());
    private final JButton btnTambah = Tema.utama("Tambah Slot");
    private final JButton btnSimpan = Tema.kedua("Simpan Perubahan");
    private final JButton btnHapus = Tema.bahaya("Hapus Slot");
    private final JButton btnBersih = Tema.kedua("Bersihkan");

    private List<SlotParkir> semuaSlot = new ArrayList<>();
    private List<SlotParkir> tampil = new ArrayList<>();
    private int idTerpilih = 0;

    public SlotPanel(MainFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout(18, 0));

        add(buatPanelTabel(), BorderLayout.CENTER);
        add(buatFormulir(), BorderLayout.EAST);
        pasangAksi();
        bersihkanForm();
    }

    // ------------------------------------------------------------- tampilan

    private JPanel buatPanelTabel() {
        JPanel kartu = Tema.kartu();
        kartu.setLayout(new BorderLayout(0, 10));

        JPanel atas = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        atas.setOpaque(false);
        txtCari.setPreferredSize(new Dimension(200, 34));
        atas.add(Tema.teks("Cari nomor"));
        atas.add(txtCari);
        kartu.add(atas, BorderLayout.NORTH);

        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = Tema.gulir(tabel);
        kartu.add(scroll, BorderLayout.CENTER);
        kartu.add(lblInfo, BorderLayout.SOUTH);
        return kartu;
    }

    private JPanel buatFormulir() {
        JPanel kartu = Tema.kartu();
        kartu.setPreferredSize(new Dimension(340, 0));
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));

        JLabel judul = Tema.subjudul("Data Slot");
        judul.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(judul);
        kartu.add(Box.createVerticalStrut(4));

        JLabel keterangan = Tema.kecil("Isi nomor, pilih jenis, lalu simpan.");
        keterangan.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(keterangan);
        kartu.add(Box.createVerticalStrut(16));

        kartu.add(Tema.berlabel("NOMOR SLOT", txtNomor));
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(Tema.berlabel("JENIS KENDARAAN", cbJenis));
        kartu.add(Box.createVerticalStrut(20));

        for (JButton tombol : new JButton[] { btnTambah, btnSimpan, btnHapus, btnBersih }) {
            tombol.setAlignmentX(LEFT_ALIGNMENT);
            tombol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            kartu.add(tombol);
            kartu.add(Box.createVerticalStrut(8));
        }

        kartu.add(Box.createVerticalStrut(8));
        JTextArea catatan = Tema.catatan("Slot yang sedang terisi tidak dapat diubah jenisnya atau "
                + "dihapus sebelum dikosongkan.", 280);
        kartu.add(catatan);
        kartu.add(Box.createVerticalGlue());
        return kartu;
    }

    // ---------------------------------------------------------------- aksi

    private void pasangAksi() {
        btnTambah.addActionListener(e -> tambah());
        btnSimpan.addActionListener(e -> ubah());
        btnHapus.addActionListener(e -> hapus());
        btnBersih.addActionListener(e -> bersihkanForm());
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                pilihBaris();
            }
        });
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                terapkanFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                terapkanFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                terapkanFilter();
            }
        });
    }

    @Override
    public void muatUlang() {
        try {
            semuaSlot = frame.slot().semua();
        } catch (DataAccessException e) {
            galat("Gagal memuat slot: " + e.getMessage());
            semuaSlot = new ArrayList<>();
        }
        terapkanFilter();
        bersihkanForm();
    }

    private void terapkanFilter() {
        String kunci = txtCari.getText().trim().toLowerCase();
        tampil = new ArrayList<>();
        List<Object[]> baris = new ArrayList<>();
        for (SlotParkir slot : semuaSlot) {
            boolean cocok = kunci.isEmpty()
                    || slot.getNomor().toLowerCase().contains(kunci)
                    || slot.getJenis().getLabel().toLowerCase().contains(kunci)
                    || slot.getStatus().getLabel().toLowerCase().contains(kunci);
            if (cocok) {
                tampil.add(slot);
                baris.add(new Object[] { slot.getNomor(), slot.getJenis().getLabel(), slot.getStatus().getLabel() });
            }
        }
        Tema.isiTabel(tabel, baris);
        lblInfo.setText("Menampilkan " + tampil.size() + " dari " + semuaSlot.size() + " slot.");
        idTerpilih = 0;
    }

    private void pilihBaris() {
        int baris = Tema.barisTerpilih(tabel);
        if (baris < 0 || baris >= tampil.size()) {
            return;
        }
        SlotParkir slot = tampil.get(baris);
        idTerpilih = slot.getId();
        txtNomor.setText(slot.getNomor());
        cbJenis.setSelectedItem(slot.getJenis());
    }

    private void tambah() {
        try {
            SlotParkir slot = frame.slot().tambah(txtNomor.getText(), jenisTerpilih(), frame.getPetugas());
            info("Slot " + slot.getNomor() + " berhasil ditambahkan.");
            muatUlang();
        } catch (ValidasiException e) {
            galat(e.getMessage());
        } catch (DataAccessException e) {
            galat("Gagal menyimpan slot: " + e.getMessage());
        }
    }

    private void ubah() {
        if (idTerpilih == 0) {
            galat("Pilih slot pada tabel terlebih dahulu.");
            return;
        }
        try {
            frame.slot().ubah(idTerpilih, txtNomor.getText(), jenisTerpilih(), frame.getPetugas());
            info("Slot berhasil diubah.");
            muatUlang();
        } catch (ValidasiException e) {
            galat(e.getMessage());
        } catch (DataAccessException e) {
            galat("Gagal mengubah slot: " + e.getMessage());
        }
    }

    private void hapus() {
        if (idTerpilih == 0) {
            galat("Pilih slot yang akan dihapus.");
            return;
        }
        int pilih = JOptionPane.showConfirmDialog(this,
                "Hapus slot " + txtNomor.getText().trim().toUpperCase() + "?\nTindakan ini tidak dapat dibatalkan.",
                "Konfirmasi Hapus Slot", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (pilih != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            frame.slot().hapus(idTerpilih, frame.getPetugas());
            info("Slot berhasil dihapus.");
            muatUlang();
        } catch (ValidasiException e) {
            galat(e.getMessage());
        } catch (DataAccessException e) {
            galat("Gagal menghapus slot: " + e.getMessage());
        }
    }

    private JenisKendaraan jenisTerpilih() {
        Object nilai = cbJenis.getSelectedItem();
        return nilai instanceof JenisKendaraan ? (JenisKendaraan) nilai : null;
    }

    private void bersihkanForm() {
        idTerpilih = 0;
        txtNomor.setText("");
        cbJenis.setSelectedIndex(0);
        tabel.clearSelection();
        txtNomor.requestFocusInWindow();
    }

    private void info(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Berhasil", JOptionPane.INFORMATION_MESSAGE);
    }

    private void galat(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Tidak dapat diproses", JOptionPane.WARNING_MESSAGE);
    }
}
