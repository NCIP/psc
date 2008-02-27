class CleanParticipantIdentifiers extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        dropTable("participant_identifiers")
        dropColumn("study_sites", "study_identifier")
        addColumn("participant_assignments", "study_id", "string")
    }

    void down() {
        dropColumn("participant_assignments", "study_id")
        addColumn("study_sites", "study_identifier", "string")

        // copied from 002/004_create_participant_identifiers
        createTable('participant_identifiers') { t ->
            t.addColumn('medical_record_number', 'string', nullable:false)
            t.addColumn('participant_id', 'integer', nullable:false)
            t.addColumn('identifier_type', 'string', nullable:false)
            t.addColumn('description', 'string', nullable:false)
            t.addColumn('site_id', 'integer', nullable:false)
            t.addColumn('version', 'integer', nullable:false)
        }
    }
}