class DropNotNullPlannedCalendarFromEpoch extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        setNullable("epochs", "planned_calendar_id", true);
    }

    void down() {
        setNullable("epochs", "planned_calendar_id", false);
    }
}
