class AddEpochs extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('epochs') { t ->
            t.addVersionColumn()
            t.addColumn('name', 'string', nullable:false)
            t.addColumn('planned_schedule_id', 'integer', nullable:false)
        }
        execute("ALTER TABLE epochs ADD CONSTRAINT fk_epoch_sched FOREIGN KEY (planned_schedule_id) REFERENCES planned_schedules");

        // create an epoch for each existing schedule
        if (databaseMatches('oracle')) {
            execute("""\
                INSERT INTO epochs (id, name, planned_schedule_id)
                    SELECT seq_epochs_id.nextval, 'Primary', ps.id
                        FROM planned_schedules ps""");
        } else {
            execute("""\
                INSERT INTO epochs (name, planned_schedule_id)
                    SELECT 'Primary', ps.id
                        FROM planned_schedules ps""");
        }

        // associate arms with epochs, not schedules
        addColumn('arms', 'epoch_id', 'integer')
        if (databaseMatches('postgresql')) {
            // postgresql's UPDATE from SELECT is not ANSI-compatible
            execute('UPDATE arms SET epoch_id=e.id FROM epochs e INNER JOIN arms a ON a.planned_schedule_id=e.planned_schedule_id WHERE arms.id=a.id');
        } else {
            execute('UPDATE arms SET epoch_id=(SELECT e.id FROM epochs e INNER JOIN arms a ON e.planned_schedule_id=a.planned_schedule_id WHERE arms.id=a.id)');
        }
        execute('ALTER TABLE arms ADD CONSTRAINT fk_arm_epoch FOREIGN KEY (epoch_id) REFERENCES epochs');
        execute('ALTER TABLE arms ADD CONSTRAINT nn_arm_epoch CHECK (epoch_id IS NOT NULL)');

        execute('ALTER TABLE arms DROP CONSTRAINT fk_arm_plan_sched');
        execute('ALTER TABLE arms DROP CONSTRAINT nn_arm_plan_sched');
        removeColumn('arms', 'planned_schedule_id')
    }

    void down() {
        // revert arms changes
        addColumn('arms', 'planned_schedule_id', 'integer')
        if (databaseMatches('postgresql')) {
            execute('UPDATE arms SET planned_schedule_id=e.planned_schedule_id FROM epochs e INNER JOIN arms a ON a.epoch_id=e.id')
        } else {
            execute('UPDATE arms SET planned_schedule_id=(SELECT e.planned_schedule_id FROM epochs e INNER JOIN arms a ON a.epoch_id=e.id)')
        }
        removeColumn('arms', 'epoch_id')

        dropTable('epochs')
    }
}