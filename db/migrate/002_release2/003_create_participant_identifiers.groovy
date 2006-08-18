class CreateParticipantIdentifiers extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('participant_identifiers') { t ->
            t.addColumn('medical_record_number', 'string', nullable:false)
	    t.addColumn('participant_id', 'integer' nullable:false)
	    t.addColumn('identifier_type', 'string', nullable:false)
	    t.addColumn('description', 'string', nullable:false)
	    t.addColumn('site_id', 'integer', nullable:false)
            t.addColumn('version', 'integer', nullable:false)
        }
    }

    void down() {
        dropTable('participant_identifiers')
    }
}