// Change ScheduledEventState to a class in its own right, with its subclasses
// the niche previously held by ScheduledEventState will be renamed ScheduledEventMode
// and is an implementation detail.
class ExpandScheduledEventState extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameTable("scheduled_event_states", "scheduled_event_modes", primaryKey: false)

        // create new state table
        createTable("scheduled_event_states") { t ->
            t.addVersionColumn()
            t.addColumn("scheduled_event_id", "integer", nullable: false)
            t.addColumn("mode_id", "integer", nullable: false, defaultValue: 1)
            t.addColumn("actual_date", "date")
            t.addColumn("reason", "string")
            t.addColumn("list_index", "integer", defaultValue: 0, nullable: false)
        }

        execute('ALTER TABLE scheduled_event_states ADD CONSTRAINT fk_sched_evt_state_evt FOREIGN KEY (scheduled_event_id) REFERENCES scheduled_events')
        execute('ALTER TABLE scheduled_event_states ADD CONSTRAINT fk_sched_evt_state_mode FOREIGN KEY (mode_id) REFERENCES scheduled_event_modes')

        renameColumn("scheduled_events", "actual_date", "current_state_date")
        renameColumn("scheduled_events", "scheduled_event_state_id", "current_state_mode_id")
        addColumn("scheduled_events", "current_state_reason", "string")
    }

    void down() {
        dropColumn("scheduled_events", "current_state_reason")
        renameColumn("scheduled_events", "current_state_date", "actual_date")
        renameColumn("scheduled_events", "current_state_mode_id", "scheduled_event_state_id")

        dropTable("scheduled_event_states")
        renameTable("scheduled_event_modes", "scheduled_event_states", primaryKey: false)
    }
}