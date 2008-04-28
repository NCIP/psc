class AddPopulationModel extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('populations') { t ->
            t.addVersionColumn()
            t.addColumn('name', 'string', nullable: false)
            t.addColumn('abbreviation', 'string', nullable: false, limit: 5)
            t.addColumn('study_id', 'integer', nullable: false, references: 'studies')
            t.addColumn('grid_id', 'string', limit: 255)
        }

        createTable('subject_populations') { t ->
            t.includePrimaryKey = false
            t.addColumn('assignment_id', 'integer', references: 'subject_assignments')
            t.addColumn('population_id', 'integer', references: 'populations')
        }

        addColumn('planned_activities', 'population_id', 'integer', references: 'populations')
    }

    void down() {
        dropColumn('planned_activities', 'population_id')
        dropTable("subject_populations", primaryKey:false)
        dropTable("populations")
    }
}