class RenameScheduledEventStatesTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameTable("scheduled_event_states", "scheduled_activity_states")
        renameColumn("scheduled_activity_states", "scheduled_event_id", "scheduled_activity_id")
    }

    void down() {
        renameTable("scheduled_activity_states", "scheduled_event_states")
        renameColumn("scheduled_event_states", "scheduled_activity_id", "scheduled_event_id")
    }
}