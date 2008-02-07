class CreateUserRoleStudySites extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        dropTable("user_study_sites", primaryKey:false)

        createTable('user_role_study_sites') { t ->
            t.includePrimaryKey = false
            t.addColumn('user_role_id', 'integer', nullable:false)
            t.addColumn('study_site_id', 'string', nullable: false)
        }

    }

    void down() {
        dropTable("user_role_study_sites", primaryKey:false)

        createTable('user_study_sites') { t ->
            t.includePrimaryKey = false
            t.addColumn('user_id', 'integer', nullable:false)
            t.addColumn('study_site_id', 'string', nullable: false)
        }

    }
}
