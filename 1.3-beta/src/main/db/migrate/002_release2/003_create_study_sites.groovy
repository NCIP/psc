class CreateStudySites extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('study_sites') { t ->
            t.addColumn('site_id', 'integer', nullable:false)
            t.addColumn('study_id', 'integer', nullable:false)
	    t.addColumn('study_identifier', 'string', nullable:false)
	    t.addColumn('version', 'integer', nullable:false)
        }
    }

    void down() {
        dropTable('study_sites')
    }
}