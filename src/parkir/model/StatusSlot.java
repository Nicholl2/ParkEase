package parkir.model;

public enum StatusSlot {

    KOSONG("Kosong"),
    TERISI("Terisi");

    private final String label;

    StatusSlot(String label) {
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
