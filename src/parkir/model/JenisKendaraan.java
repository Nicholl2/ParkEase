package parkir.model;

public enum JenisKendaraan {

    MOTOR("Motor"),
    MOBIL("Mobil");

    private final String label;

    JenisKendaraan(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static JenisKendaraan dari(String teks) {
        for (JenisKendaraan jenis : values()) {
            if (jenis.name().equalsIgnoreCase(teks) || jenis.label.equalsIgnoreCase(teks)) {
                return jenis;
            }
        }
        throw new IllegalArgumentException("Jenis kendaraan tidak dikenal: " + teks);
    }

    @Override
    public String toString() {
        return label;
    }
}
