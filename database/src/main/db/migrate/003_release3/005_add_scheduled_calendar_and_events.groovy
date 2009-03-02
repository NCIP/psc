class AddScheduledCalendarAndEvents extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("scheduled_calendars") { t ->
            t.addVersionColumn()
            t.addColumn('assignment_id',  'integer', nullable: false)
        }
        execute('ALTER TABLE scheduled_calendars ADD CONSTRAINT fk_sched_cal_assign FOREIGN KEY (assignment_id) REFERENCES participant_assignments')

        // join table between scheduled_calendar and arms
        createTable("scheduled_arms") { t ->
            t.includePrimaryKey = false
            t.addColumn('scheduled_calendar_id', 'integer', nullable: false)
            t.addColumn('arm_id',                'integer', nullable: false)
            t.addColumn('list_index',            'integer', nullable: false)
        }
        execute('ALTER TABLE scheduled_arms ADD CONSTRAINT fk_sched_arm_cal FOREIGN KEY (scheduled_calendar_id) REFERENCES scheduled_calendars')
        execute('ALTER TABLE scheduled_arms ADD CONSTRAINT fk_sched_arm_arm FOREIGN KEY (arm_id) REFERENCES arms')

        createTable("scheduled_event_states") { t ->
            // no autoincrementing key because we need to be able to refer to these states
            // consistently from code and other SQL statements
            t.includePrimaryKey = false
            t.addColumn('id', 'integer', primaryKey: true)
            t.addColumn('name', 'string', nullable: false)
        }
        insert("scheduled_event_states", [ id: 1, name: "scheduled" ], primaryKey: false)
        insert("scheduled_event_states", [ id: 2, name: "occurred"  ], primaryKey: false)
        insert("scheduled_event_states", [ id: 3, name: "canceled"  ], primaryKey: false)

        createTable("scheduled_events") { t ->
            t.addVersionColumn()
            t.addColumn('scheduled_calendar_id',    'integer', nullable: false)
            t.addColumn('planned_event_id',         'integer', nullable: false)
            t.addColumn('ideal_date',               'date', nullable: false)
            t.addColumn('scheduled_event_state_id', 'integer', nullable: false, defaultValue: 1)
            t.addColumn('actual_date',              'date')
            t.addColumn('notes',                    'string')
        }
        execute('ALTER TABLE scheduled_events ADD CONSTRAINT fk_sched_evt_cal FOREIGN KEY (scheduled_calendar_id) REFERENCES scheduled_calendars')
        execute('ALTER TABLE scheduled_events ADD CONSTRAINT fk_sched_evt_evt FOREIGN KEY (planned_event_id) REFERENCES planned_events')
        execute('ALTER TABLE scheduled_events ADD CONSTRAINT fk_sched_evt_state FOREIGN KEY (scheduled_event_state_id) REFERENCES scheduled_event_states')
    }

    void down() {
        dropTable("scheduled_events")
        dropTable("scheduled_event_states", primaryKey: false)
        dropTable("scheduled_arms", primaryKey: false)
        dropTable("scheduled_calendars")
    }
}