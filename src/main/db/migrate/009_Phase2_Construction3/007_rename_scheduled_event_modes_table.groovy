class RenameScheduledEventModesTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameTable("scheduled_event_modes", "scheduled_activity_modes", primaryKey: false)
    }

    void down() {
        renameTable("scheduled_activity_modes", "scheduled_event_modes", primaryKey: false)
    }
}
