class ScheduledEventRenamePlannedEvent extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameColumn("scheduled_events", "planned_event_id", "planned_activity_id")
    }

    void down() {
        renameColumn("scheduled_events", "planned_activity_id", "planned_event_id")
    }
}
