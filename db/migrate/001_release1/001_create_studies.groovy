class CreateStudies extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('studies') { t ->
            t.addColumn('name', 'string', nullable:false)
            t.addColumn('comments', 'string')
        }
    }

    void down() {
        dropTable('studies')
    }
}
