package parkir.model;

import parkir.exception.ValidasiException;
import parkir.util.Money;

public class PembayaranQris implements MetodePembayaran {

    @Override
    public String getKode() {
        return "QRIS";
    }

    @Override
    public String getLabel() {
        return "QRIS (simulasi)";
    }

    @Override
    public String proses(long jumlah) throws ValidasiException {
        MetodePembayaran.validasiJumlah(jumlah);
        return "Pembayaran QRIS simulasi " + Money.format(jumlah) + " berhasil.";
    }
}
