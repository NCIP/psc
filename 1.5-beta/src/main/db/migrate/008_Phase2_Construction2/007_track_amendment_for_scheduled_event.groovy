class TrackAmendmentForScheduledEvent extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("scheduled_events", "source_amendment_id", "integer");
        execute("UPDATE scheduled_events SET source_amendment_id=(SELECT spa.current_amendment_id \n" 
            + "  FROM participant_assignments spa"
            + "    INNER JOIN scheduled_calendars sc ON spa.id=sc.assignment_id"
            + "    INNER JOIN scheduled_arms sa ON sa.scheduled_calendar_id=sc.id"
            + "  WHERE sa.id=scheduled_arm_id);");

        execute("ALTER TABLE scheduled_events ADD CONSTRAINT fk_sched_evt_amendment FOREIGN KEY (source_amendment_id) REFERENCES amendments");
        setNullable("scheduled_events", "source_amendment_id", false);
    }

    void down() {
        dropColumn("scheduled_events", "source_amendment_id")
    }
}
