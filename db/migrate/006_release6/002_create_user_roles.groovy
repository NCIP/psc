class CreateUserRoles extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('User_Roles') { t ->
            t.includePrimaryKey = false
            t.addColumn('csm_group_name', 'string', nullable: false)
            t.addColumn('user_id', 'integer', nullable:false)
        }
    }

    void down() {
        dropTable('User_Roles', primaryKey: false))

    }
}
