class AddActivitySource extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('sources') { t ->
            t.addColumn('name', 'string', nullable:false, limit: 255)
            t.addColumn('version', 'integer', nullable:false)
            t.addColumn('grid_id', 'string', nullable:true, limit: 255)
        }

        addColumn('activities', 'source_id', 'integer')
        addColumn('activities', 'code', 'string', limit: 255)
    }

    void down() {
        dropColumn('activities', 'source_id')
        dropColumn('activities', 'code')
        dropTable('sources')
    }
}
