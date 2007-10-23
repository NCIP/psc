class TrackAmendmentForAssignment extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("participant_assignments", "current_amendment_id", "integer");
        execute("UPDATE participant_assignments SET current_amendment_id=(SELECT amendment_id FROM studies s INNER JOIN study_sites ss ON s.id=ss.study_id WHERE ss.id=study_site_id);");

        execute("ALTER TABLE participant_assignments ADD CONSTRAINT fk_schedule_amendment FOREIGN KEY (current_amendment_id) REFERENCES amendments");
        setNullable("participant_assignments", "current_amendment_id", false);
    }

    void down() {
        dropColumn("participant_assignments", "current_amendment_id")
    }
}
