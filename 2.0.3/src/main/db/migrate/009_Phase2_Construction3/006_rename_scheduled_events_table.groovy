class RenameScheduledEventsTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameTable("scheduled_events", "scheduled_activities")
    }

    void down() {
        renameTable("scheduled_activities", "scheduled_events")
    }
}