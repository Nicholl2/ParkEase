package parkir.model;

import parkir.exception.ValidasiException;
import parkir.util.Money;

public class PembayaranTunai implements MetodePembayaran {

    @Override
    public String getKode() {
        return "TUNAI";
    }

    @Override
    public String getLabel() {
        return "Tunai";
    }

    @Override
    public String proses(long jumlah) throws ValidasiException {
        MetodePembayaran.validasiJumlah(jumlah);
        return "Pembayaran tunai diterima " + Money.format(jumlah) + ".";
    }
}
