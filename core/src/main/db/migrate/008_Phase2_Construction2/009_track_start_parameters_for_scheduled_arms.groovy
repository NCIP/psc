class TrackStartParametersForScheduledArms extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("scheduled_arms", "start_date", "date")
        addColumn("scheduled_arms", "start_day", "integer")

        execute(
            "UPDATE scheduled_arms\n"
            + "  SET start_date=(SELECT MIN(ideal_date) FROM scheduled_events se\n"
            + "                    WHERE se.scheduled_arm_id=scheduled_arms.id AND se.planned_event_id IS NOT NULL),\n"
            + "      start_day= (SELECT MIN(pe.day) FROM planned_events pe\n"
            + "                    WHERE pe.id\n"
            + "                      IN (SELECT se.planned_event_id FROM scheduled_events se WHERE se.scheduled_arm_id=scheduled_arms.id))"
            );
        execute("DELETE FROM scheduled_events WHERE scheduled_arm_id IN (SELECT id FROM scheduled_arms WHERE start_date IS NULL OR start_day IS NULL)")
        execute("DELETE FROM scheduled_arms WHERE start_date IS NULL OR start_day IS NULL")
        setNullable("scheduled_arms", "start_date", false);
        setNullable("scheduled_arms", "start_day", false);
    }

    void down() {
        dropColumn("scheduled_arms", "start_date")
        dropColumn("scheduled_arms", "start_day")
    }
}
