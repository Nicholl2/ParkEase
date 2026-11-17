package parkir.model;

import parkir.exception.ValidasiException;

public class SlotParkir {

    private final int id;
    private final String nomor;
    private final JenisKendaraan jenis;
    private StatusSlot status;

    public SlotParkir(int id, String nomor, JenisKendaraan jenis, StatusSlot status) throws ValidasiException {
        if (nomor == null || nomor.isBlank()) {
            throw new ValidasiException("Nomor slot wajib diisi.");
        }
        if (jenis == null) {
            throw new ValidasiException("Jenis slot wajib dipilih.");
        }
        this.id = id;
        this.nomor = nomor.trim().toUpperCase();
        this.jenis = jenis;
        this.status = status == null ? StatusSlot.KOSONG : status;
    }

    public int getId() {
        return id;
    }

    public String getNomor() {
        return nomor;
    }

    public JenisKendaraan getJenis() {
        return jenis;
    }

    public StatusSlot getStatus() {
        return status;
    }

    public boolean isKosong() {
        return status == StatusSlot.KOSONG;
    }

    public void tempati() throws ValidasiException {
        if (status == StatusSlot.TERISI) {
            throw new ValidasiException("Slot " + nomor + " sudah terisi.");
        }
        status = StatusSlot.TERISI;
    }

    public void lepaskan() {
        status = StatusSlot.KOSONG;
    }

    @Override
    public String toString() {
        return nomor + " (" + jenis.getLabel() + ", " + status.getLabel() + ")";
    }
}
