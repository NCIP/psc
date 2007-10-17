class CreateUserRoleEntityAndUserUserRoleSite extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        // drop current user_roles table
        dropTable('user_roles', primaryKey:false)

        // add new user_roles table with surrogate primary key
        createTable('user_roles') { t ->
            t.addVersionColumn()
            t.addColumn('user_id', 'integer', nullable:false)
            t.addColumn('csm_group_name', 'string', nullable: false)
            t.addColumn("grid_id", "string", nullable: true)
        }

        // create user_role_sites to relate user_roles to site, if there are any
        createTable('user_role_sites') { t ->
            t.includePrimaryKey = false
            t.addColumn('user_role_id', 'integer', nullable: false)
            t.addColumn('site_id', 'integer', nullable:false)
        }

        // on user_role_sites add user_role_id and site_id as primary keys
        execute("ALTER TABLE user_role_sites ADD PRIMARY KEY (user_role_id, site_id)")
    }

    void down() {
        dropTable("user_role_sites", primaryKey:false)

        dropTable("user_roles", primaryKey:true)

        createTable('user_roles') { t ->
            t.includePrimaryKey = false
            t.addColumn('csm_group_name', 'string', nullable: false)
            t.addColumn('user_id', 'integer', nullable:false)

        }

        execute("ALTER TABLE user_roles ADD PRIMARY KEY (csm_group_name, user_id)")
    }
}