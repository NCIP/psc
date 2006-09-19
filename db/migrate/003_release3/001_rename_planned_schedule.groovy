class RenamePlannedSchedule extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("ALTER TABLE planned_schedules RENAME TO planned_calendars")
        if (databaseMatches('oracle')) {
            execute("RENAME seq_planned_schedules_id TO seq_planned_calendars_id");
        }

        if (databaseMatches('hsqldb')) {
            execute("ALTER TABLE epochs ALTER COLUMN planned_schedule_id RENAME TO planned_calendar_id");
        } else {
            execute("ALTER TABLE epochs RENAME COLUMN planned_schedule_id TO planned_calendar_id");
        }
    }

    void down() {
        if (databaseMatches('hsqldb')) {
            execute("ALTER TABLE epochs ALTER COLUMN planned_calendar_id RENAME TO planned_schedule_id");
        } else {
            execute("ALTER TABLE epochs RENAME COLUMN planned_calendar_id TO planned_schedule_id");
        }

        if (databaseMatches('oracle')) {
            execute("RENAME seq_planned_calendars_id TO seq_planned_schedules_id");
        }
        execute("ALTER TABLE planned_calendars RENAME TO planned_schedules")
    }
}