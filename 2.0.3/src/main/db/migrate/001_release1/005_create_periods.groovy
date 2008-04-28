class CreatePeriods extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('periods') { t ->
            t.addColumn('name', 'string', nullable:false)
            t.addColumn('arm_id', 'integer', nullable:false)
            t.addColumn('start_day', 'integer', nullable:false)
            t.addColumn('duration_quantity', 'integer', nullable:false)
            t.addColumn('duration_unit', 'string', nullable:false)
            t.addColumn('repetitions', 'integer', nullable:false, defaultValue:1)
            t.addColumn('version', 'integer', nullable:false, defaultValue:0)
        }
    }

    void down() {
        dropTable('periods')
    }
}
