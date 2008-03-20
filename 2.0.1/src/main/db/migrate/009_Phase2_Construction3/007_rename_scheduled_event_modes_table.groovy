class RenameScheduledEventModesTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("ALTER TABLE scheduled_event_modes RENAME TO scheduled_activity_modes")
    }

    void down() {
        execute("ALTER TABLE scheduled_activity_modes RENAME TO scheduled_event_modes")
    }
}