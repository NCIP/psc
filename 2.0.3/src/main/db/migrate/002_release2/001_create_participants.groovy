class CreateParticipants extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('participants') { t ->
            t.addColumn('first_name', 'string', nullable:false, limit: 255)
            t.addColumn('last_name', 'string', nullable:false, limit: 255)
            t.addColumn('birth_date', 'date', nullable:false)
            t.addColumn('gender', 'string', nullable:false)
            t.addColumn('social_security_number', 'string', nullable:false, limit: 255)
            t.addColumn('version', 'integer', nullable:false)
        }
    }

    void down() {
        dropTable('participants')
    }
}
