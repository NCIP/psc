class ExplicitlyOrderArmsAndEpochs extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("epochs", "list_index", "integer", defaultValue: 0, nullable: false)
        addColumn("arms", "list_index", "integer", defaultValue: 0, nullable: false)
    }

    void down() {
        dropColumn("arms", "list_index");
        dropColumn("epochs", "list_index");
    }
}