class ModifyPlannedEvents extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("planned_events", "conditional", "boolean", defaultValue:"0");
        addColumn("planned_events", "conditional_details", "string")
    }

    void down() {
        dropColumn("planned_events", "conditional");
        dropColumn("planned_events", "conditional_details")
    }
}