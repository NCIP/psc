class ImplementNonmandatoryAmendments extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("amendments", "mandatory", "boolean", defaultValue: "1")
    }

    void down() {
        dropColumn("amendments", "mandatory")
    }
}
