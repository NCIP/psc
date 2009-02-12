class ModifyPlannedEventTables extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameTable("planned_events", "planned_activities")
    }

    void down() {
        renameTable("planned_activities", "planned_events")
    }
}