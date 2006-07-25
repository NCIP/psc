class CreateActivities extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('activities') { t ->
            t.addColumn('version', 'integer', nullable:false)
            t.addColumn('name', 'string', nullable:false)
            t.addColumn('type', 'string', nullable:false)
        }
        
        createTable('activity_types') { t ->
            t.addColumn('version', 'integer', nullable:false)
            t.addColumn('name', 'string', nullable:false)
        }
    }

    void down() {
        dropTable('activities')
        dropTable('activity_types')
    }
}
