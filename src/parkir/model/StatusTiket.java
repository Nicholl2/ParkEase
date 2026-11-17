package parkir.model;

/** Siklus hidup tiket. Overstay bukan status, melainkan flag terpisah. */
public enum StatusTiket {

    AKTIF("Aktif"),
    SELESAI("Selesai"),
    DIBATALKAN("Dibatalkan");

    private final String label;

    StatusTiket(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
