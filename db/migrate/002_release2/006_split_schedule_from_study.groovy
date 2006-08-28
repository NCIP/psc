class CreateStudyParticipantAssignments extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        // create main table
        createTable('planned_schedules') { t ->
            t.addColumn('study_id', 'integer', nullable:false)
            // default 0 has to be quoted for postgresql
            t.addColumn('complete', 'boolean', nullable:false, defaultValue:"'0'")
            t.addColumn('version', 'integer', nullable:false)
        }

        // Create planned schedules for all existing studies
        if (databaseMatches('oracle')) {
            execute("""\
                INSERT INTO planned_schedules (id, study_id, version, complete)
                    SELECT seq_planned_schedules_id.nextval, id, 0, completed FROM studies""");
        } else {
            execute('INSERT INTO planned_schedules (study_id, version) SELECT id, 0 FROM studies');
        }
        // constrain
        execute('ALTER TABLE planned_schedules ADD CONSTRAINT fk_plan_sched_study FOREIGN KEY (study_id) REFERENCES studies');

        // remove completed column from studies (it's an attribute of the schedule)
        removeColumn('studies', 'completed');

        // shift arms' reference from study to planned schedule
        addColumn('arms', 'planned_schedule_id', 'integer');
        if (databaseMatches('postgresql')) {
            // postgresql's UPDATE from SELECT is not ANSI-compatible
            execute('UPDATE arms SET planned_schedule_id=ps.id FROM planned_schedules ps INNER JOIN arms a ON a.study_id=ps.study_id');
        } else {
            execute('UPDATE arms SET planned_schedule_id=(SELECT ps.id FROM planned_schedules ps INNER JOIN arms a ON ps.study_id=a.study_id)');
        }
        removeColumn('arms', 'study_id');
        execute('ALTER TABLE arms ADD CONSTRAINT fk_arm_plan_sched FOREIGN KEY (planned_schedule_id) REFERENCES planned_schedules');
        execute('ALTER TABLE arms ADD CONSTRAINT nn_arm_plan_sched CHECK (planned_schedule_id IS NOT NULL)');
    }

    void down() {
        // reverse change to studies
        addColumn('studies', 'completed', 'boolean');
        if (databaseMatches('postgresql')) {
            execute('UPDATE studies SET completed=ps.complete FROM planned_schedules ps INNER JOIN studies s ON ps.study_id=s.id');
        } else {
            execute('UPDATE studies SET completed=(SELECT ps.complete FROM planned_schedules ps INNER JOIN studies s ON ps.study_id=s.id)');
        }

        // reverse change to arms
        addColumn('arms', 'study_id', 'integer');
        if (databaseMatches('postgresql')) {
            execute('UPDATE arms SET study_id=ps.study_id FROM planned_schedules ps INNER JOIN arms a ON ps.id=a.planned_schedule_id');
        } else {
            execute('UPDATE arms SET study_id=(SELECT a.study_id FROM planned_schedules ps INNER JOIN arms a ON ps.id=a.planned_schedule_id)');
        }
        execute('ALTER TABLE arms ADD CONSTRAINT fk_arm_study FOREIGN KEY (study_id) REFERENCES studies');
        execute('ALTER TABLE arms DROP COLUMN planned_schedule_id');

        // drop main table
        dropTable('planned_schedules')
    }
}