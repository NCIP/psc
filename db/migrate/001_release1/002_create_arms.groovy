class CreateArms extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('arms') { t ->
            t.addColumn('version', 'integer', nullable:false)
            t.addColumn('name', 'string', nullable:false)
            t.addColumn('study_id', 'integer', nullable:false)
            t.addColumn('number', 'integer', nullable:false)
        }
    }

    void down() {
        dropTable('arms')
    }
}
