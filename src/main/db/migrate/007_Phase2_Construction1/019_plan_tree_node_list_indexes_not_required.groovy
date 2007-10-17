class PlanTreeNodeListIndexesNotRequired extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        setNullable("epochs", "list_index", true);
        setNullable("arms", "list_index", true);
    }

    void down() {
        setNullable("epochs", "list_index", false);
        setNullable("arms", "list_index", false);
    }
}
