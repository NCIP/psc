class ModifyPlannedEvents extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        dropColumn("planned_events", "conditional");
    }

    void down() {
        addColumn("planned_events", "conditional", "boolean", defaultValue:"0");
    }
}