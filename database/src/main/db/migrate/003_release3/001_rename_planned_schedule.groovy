class RenamePlannedSchedule extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameTable("planned_schedules", "planned_calendars", primaryKey: true)
        renameColumn("epochs", "planned_schedule_id", "planned_calendar_id");
    }

    void down() {
        renameColumn("epochs", "planned_calendar_id", "planned_schedule_id");
        renameTable("planned_calendars", "planned_schedules", primaryKey: true)
    }
}
