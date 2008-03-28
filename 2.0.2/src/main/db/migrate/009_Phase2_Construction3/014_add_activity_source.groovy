class AddActivitySource extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('sources') { t ->
            t.addColumn('name', 'string', nullable:false)
            t.addColumn('version', 'integer', nullable:false)
            t.addColumn('grid_id', 'string', nullable:true)
        }

        addColumn('activities', 'source_id', 'integer')
        addColumn('activities', 'code', 'string')
    }

    void down() {
        dropColumn('activities', 'source_id')
        dropColumn('activities', 'code')
        dropTable('sources')
    }
    }