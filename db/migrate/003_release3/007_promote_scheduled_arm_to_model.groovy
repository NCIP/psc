class PromoteScheduledArmToModel extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        // ideally, this would preserve the data from the existing table, but there isn't any yet
        dropTable("scheduled_arms", primaryKey: false)
        createTable("scheduled_arms") { t ->
            t.addVersionColumn()
            t.addColumn("scheduled_calendar_id", "integer", nullable: false)
            t.addColumn("arm_id", "integer", nullable: false)
            t.addColumn("list_index", "integer", nullable: false, defaultValue: 0)
        }
        execute('ALTER TABLE scheduled_arms ADD CONSTRAINT fk_sched_arm_cal FOREIGN KEY (scheduled_calendar_id) REFERENCES scheduled_calendars')
        execute('ALTER TABLE scheduled_arms ADD CONSTRAINT fk_sched_arm_arm FOREIGN KEY (arm_id) REFERENCES arms')

        // There shouldn't be anything in this table right now.  Make sure:
        execute("DELETE FROM scheduled_events")

        execute("ALTER TABLE scheduled_events DROP CONSTRAINT fk_sched_evt_cal")
        removeColumn("scheduled_events", "scheduled_calendar_id")

        addColumn("scheduled_events", "scheduled_arm_id", "integer")
        // DdlUtils has a problem with adding constrained columns under PostgreSQL (it tries to drop an recreate the table)
        setNullable("scheduled_events", "scheduled_arm_id", false)
        execute('ALTER TABLE scheduled_events ADD CONSTRAINT fk_sched_evt_arm FOREIGN KEY (scheduled_arm_id) REFERENCES scheduled_arms')
    }

    void down() {
        execute('ALTER TABLE scheduled_events DROP CONSTRAINT fk_sched_evt_arm')
        removeColumn("scheduled_arms", "id")
        if (databaseMatches("postgresql")) {
            execute("DROP SEQUENCE scheduled_arms_id_seq")
        } else if (databaseMatches("oracle")) {
            execute("DROP SEQUENCE seq_scheduled_arms_id")
        }

        // Since we are readding a nullable column, we need to clear out the data
        execute("DELETE FROM scheduled_events")

        removeColumn("scheduled_events", "scheduled_arm_id")
        addColumn("scheduled_events", "scheduled_calendar_id", "integer")
        // DdlUtils has a problem with adding constrained columns under PostgreSQL (it tries to drop an recreate the table)
        setNullable("scheduled_events", "scheduled_calendar_id", false)
        execute('ALTER TABLE scheduled_events ADD CONSTRAINT fk_sched_evt_cal FOREIGN KEY (scheduled_calendar_id) REFERENCES scheduled_calendars')
    }
}