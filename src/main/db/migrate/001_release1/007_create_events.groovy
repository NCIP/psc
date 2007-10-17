class CreateEvents extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('planned_events') { t ->
            t.addColumn('version', 'integer', nullable:false)
            t.addColumn('activity_id', 'integer', nullable:false)
            t.addColumn('period_id', 'integer', nullable:false)
            t.addColumn('day', 'integer', nullable:false)
        }

        execute("ALTER TABLE planned_events ADD CONSTRAINT fk_event_activity FOREIGN KEY (activity_id) REFERENCES activities");
        execute("ALTER TABLE planned_events ADD CONSTRAINT fk_event_period FOREIGN KEY (period_id) REFERENCES periods");
    }

    void down() {
        dropTable('planned_events')
    }
}
