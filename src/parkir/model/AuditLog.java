package parkir.model;

import java.time.LocalDateTime;

public class AuditLog {

    private final int id;
    private final LocalDateTime waktu;
    private final int petugasId;
    private final String petugasNama;
    private final String aksi;
    private final String entitas;
    private final String entitasId;
    private final String detail;

    public AuditLog(int id, LocalDateTime waktu, int petugasId, String petugasNama, String aksi,
            String entitas, String entitasId, String detail) {
        this.id = id;
        this.waktu = waktu;
        this.petugasId = petugasId;
        this.petugasNama = petugasNama;
        this.aksi = aksi;
        this.entitas = entitas;
        this.entitasId = entitasId;
        this.detail = detail;
    }

    public int getId() {
        return id;
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

    public String getAksi() {
        return aksi;
    }

    public String getEntitas() {
        return entitas;
    }

    public String getEntitasId() {
        return entitasId;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return waktu + " | " + (petugasNama == null ? "sistem" : petugasNama) + " | " + aksi
                + (detail == null ? "" : " | " + detail);
    }
}
