class AddIndexUserRoles extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
         dropTable('user_roles')
         createTable('user_roles') { t ->
            t.addColumn('csm_group_name', 'string', nullable: false)
            t.addColumn('user_id', 'integer', nullable:false)
        }
    }

    void down() {
         dropTable('user_roles')

         createTable('user_roles') { t ->
            t.includePrimaryKey = false
            t.addColumn('csm_group_name', 'string', nullable: false)
            t.addColumn('user_id', 'integer', nullable:false)
        }

    }
}
