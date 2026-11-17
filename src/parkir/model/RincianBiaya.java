package parkir.model;

import parkir.util.Money;

/** Hasil perhitungan biaya satu transaksi. Immutable. */
public final class RincianBiaya {

    private final int durasiJam;
    private final long tarifDasar;
    private final long dendaOverstay;
    private final long dendaTiketHilang;
    private final boolean overstay;
    private final boolean tiketHilang;

    public RincianBiaya(int durasiJam, long tarifDasar, long dendaOverstay, long dendaTiketHilang,
            boolean overstay, boolean tiketHilang) {
        this.durasiJam = durasiJam;
        this.tarifDasar = tarifDasar;
        this.dendaOverstay = dendaOverstay;
        this.dendaTiketHilang = dendaTiketHilang;
        this.overstay = overstay;
        this.tiketHilang = tiketHilang;
    }

    public int getDurasiJam() {
        return durasiJam;
    }

    public long getTarifDasar() {
        return tarifDasar;
    }

    public long getDendaOverstay() {
        return dendaOverstay;
    }

    public long getDendaTiketHilang() {
        return dendaTiketHilang;
    }

    public long getTotal() {
        return tarifDasar + dendaOverstay + dendaTiketHilang;
    }

    public boolean isOverstay() {
        return overstay;
    }

    public boolean isTiketHilang() {
        return tiketHilang;
    }

    public String ringkasan() {
        StringBuilder teks = new StringBuilder();
        teks.append("Durasi          : ").append(durasiJam).append(" jam\n");
        teks.append("Tarif dasar     : ").append(Money.format(tarifDasar)).append('\n');
        if (dendaOverstay > 0) {
            teks.append("Denda overstay  : ").append(Money.format(dendaOverstay)).append('\n');
        }
        if (dendaTiketHilang > 0) {
            teks.append("Denda tiket hilang: ").append(Money.format(dendaTiketHilang)).append('\n');
        }
        teks.append("Total           : ").append(Money.format(getTotal()));
        return teks.toString();
    }

    @Override
    public String toString() {
        return ringkasan();
    }
}
