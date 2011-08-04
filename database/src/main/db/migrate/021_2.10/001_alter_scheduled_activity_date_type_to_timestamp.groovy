class AlterScheduledActivityDateTypeToTimestamp extends edu.northwestern.bioinformatics.bering.Migration {
    @Override
    void up() {
        if (databaseMatches('oracle')) {
            execute('ALTER TABLE scheduled_activity_states MODIFY actual_date timestamp')
            execute('ALTER TABLE scheduled_activities MODIFY current_state_date timestamp')
        } else if (databaseMatches('postgresql')) {
            execute("ALTER TABLE scheduled_activity_states ALTER COLUMN actual_date TYPE timestamp")
            execute("ALTER TABLE scheduled_activities ALTER COLUMN current_state_date TYPE timestamp")
        } else {
		    execute('ALTER TABLE scheduled_activity_states ALTER COLUMN actual_date timestamp')
		    execute('ALTER TABLE scheduled_activities ALTER COLUMN current_state_date timestamp')
	    }
    }

    @Override
    void down() {
        if (databaseMatches('oracle')) {
            execute('ALTER TABLE scheduled_activity_states MODIFY actual_date date')
            execute('ALTER TABLE scheduled_activities MODIFY current_state_date date')
        } else if (databaseMatches('postgresql')) {
            execute("ALTER TABLE scheduled_activity_states ALTER COLUMN actual_date TYPE date")
            execute("ALTER TABLE scheduled_activities ALTER COLUMN current_state_date TYPE date")
        } else {
		    execute('ALTER TABLE scheduled_activity_states ALTER COLUMN actual_date date')
		    execute('ALTER TABLE scheduled_activities ALTER COLUMN current_state_date date')
        }
    }
}