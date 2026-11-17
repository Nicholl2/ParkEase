package parkir.model;

import java.time.LocalDateTime;

public class Pembayaran {

    private final int id;
    private final int tiketId;
    private final String metode;
    private final long jumlah;
    private final LocalDateTime waktu;
    private final int petugasId;
    private final String petugasNama;

    public Pembayaran(int id, int tiketId, String metode, long jumlah, LocalDateTime waktu,
            int petugasId, String petugasNama) {
        this.id = id;
        this.tiketId = tiketId;
        this.metode = metode;
        this.jumlah = jumlah;
        this.waktu = waktu;
        this.petugasId = petugasId;
        this.petugasNama = petugasNama;
    }

    public int getId() {
        return id;
    }

    public int getTiketId() {
        return tiketId;
    }

    public String getMetode() {
        return metode;
    }

    public long getJumlah() {
        return jumlah;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public int getPetugasId() {
        return petugasId;
    }

    public String getPetugasNama() {
        return petugasNama;
    }
}
