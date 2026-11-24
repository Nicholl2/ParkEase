package parkir.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import parkir.exception.DataAccessException;
import parkir.model.JenisKendaraan;
import parkir.model.RincianBiaya;
import parkir.model.StatusTiket;
import parkir.model.Tiket;
import parkir.util.Jam;
import parkir.util.Money;

/** Ringkasan kapasitas, tiket aktif, dan pendapatan hari ini. */
public class DashboardPanel extends JPanel implements MuatUlang {

    private static final long serialVersionUID = 1L;

    private final MainFrame frame;

    private final JLabel nilaiMotor = Tema.angka("0 / 0");
    private final JLabel nilaiMobil = Tema.angka("0 / 0");
    private final JLabel nilaiAktif = Tema.angka("0");
    private final JLabel nilaiPendapatan = Tema.angka("Rp0");
    private final JTable tabel = Tema.tabel("Nomor Tiket", "Slot", "Jenis", "Masuk", "Durasi", "Estimasi");

    public DashboardPanel(MainFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        JPanel statistik = new JPanel(new GridLayout(1, 4, 16, 0));
        statistik.setOpaque(false);
        nilaiMotor.setForeground(Tema.CYAN);
        nilaiMobil.setForeground(Tema.PRIMARY_HOVER);
        nilaiAktif.setForeground(Tema.UNGU);
        nilaiPendapatan.setForeground(Tema.SUKSES);
        statistik.add(Tema.kartuStatistik("Motor terisi", nilaiMotor, "dari total slot motor"));
        statistik.add(Tema.kartuStatistik("Mobil terisi", nilaiMobil, "dari total slot mobil"));
        statistik.add(Tema.kartuStatistik("Tiket aktif", nilaiAktif, "kendaraan di dalam area"));
        statistik.add(Tema.kartuStatistik("Pendapatan hari ini", nilaiPendapatan, "tiket selesai hari ini"));
        add(statistik, BorderLayout.NORTH);

        JPanel bawah = Tema.kartu();
        bawah.setLayout(new BorderLayout(0, 14));

        JPanel kepala = new JPanel(new BorderLayout());
        kepala.setOpaque(false);
        kepala.add(Tema.subjudul("Kendaraan sedang parkir"), BorderLayout.WEST);
        kepala.add(Tema.kecil("Estimasi memakai tarif snapshot masing-masing tiket"), BorderLayout.EAST);
        bawah.add(kepala, BorderLayout.NORTH);

        int[] lebar = { 150, 80, 80, 140, 80, 120 };
        for (int i = 0; i < lebar.length; i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        JScrollPane scroll = Tema.gulir(tabel);
        bawah.add(scroll, BorderLayout.CENTER);

        add(bawah, BorderLayout.CENTER);
    }

    @Override
    public void muatUlang() {
        try {
            Map<JenisKendaraan, int[]> rekap = frame.parkir().kapasitas();
            int[] motor = rekap.getOrDefault(JenisKendaraan.MOTOR, new int[] { 0, 0 });
            int[] mobil = rekap.getOrDefault(JenisKendaraan.MOBIL, new int[] { 0, 0 });
            nilaiMotor.setText(motor[0] + " / " + motor[1]);
            nilaiMobil.setText(mobil[0] + " / " + mobil[1]);

            List<Tiket> aktif = frame.parkir().daftarAktif();
            nilaiAktif.setText(String.valueOf(aktif.size()));

            List<Object[]> baris = new ArrayList<>();
            for (Tiket tiket : aktif) {
                RincianBiaya rincian = tiket.hitungRincian();
                baris.add(new Object[] {
                        tiket.getNomorTiket(),
                        tiket.getSlot().getNomor(),
                        tiket.getJenis().getLabel(),
                        Tema.waktu(tiket.getWaktuMasuk()),
                        rincian.getDurasiJam() + " jam",
                        Money.format(rincian.getTotal())
                });
            }
            Tema.isiTabel(tabel, baris);
            nilaiPendapatan.setText(Money.format(pendapatanHariIni()));
        } catch (DataAccessException e) {
            nilaiMotor.setText("—");
            nilaiMobil.setText("—");
            nilaiAktif.setText("—");
            nilaiPendapatan.setText("—");
        }
    }

    private long pendapatanHariIni() {
        return frame.parkir().cariRiwayat(null, StatusTiket.SELESAI, null).stream()
                .filter(t -> t.getWaktuKeluar() != null
                        && t.getWaktuKeluar().toLocalDate().equals(Jam.sekarang().toLocalDate()))
                .mapToLong(Tiket::getTotal)
                .sum();
    }
}
