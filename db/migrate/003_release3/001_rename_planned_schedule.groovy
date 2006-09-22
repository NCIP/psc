class RenamePlannedSchedule extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("ALTER TABLE planned_schedules RENAME TO planned_calendars")
        if (databaseMatches('oracle')) {
            execute("RENAME seq_planned_schedules_id TO seq_planned_calendars_id");
        }

        renameColumn("epochs", "planned_schedule_id", "planned_calendar_id");
    }

    void down() {
        renameColumn("epochs", "planned_calendar_id", "planned_schedule_id");

        if (databaseMatches('oracle')) {
            execute("RENAME seq_planned_calendars_id TO seq_planned_schedules_id");
        }
        execute("ALTER TABLE planned_calendars RENAME TO planned_schedules")
    }
}