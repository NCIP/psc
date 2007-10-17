class FixCsmGroupRolePgAssociations extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        // Fix Participant coordinator group access
        execute("UPDATE csm_user_group_role_pg SET role_id=4 WHERE user_group_role_pg_id=34");

        // Remove super user group from being associated with all protection groups
        execute("DELETE from csm_user_group_role_pg where user_group_role_pg_id >= 35 and user_group_role_pg_id <= 38")
        execute("DELETE from csm_user_group_role_pg where user_group_role_pg_id = 42")

        // Remove super user Group
        execute("DELETE from csm_group where group_id = 4")

    }

    void down() {
        insert( 'csm_group',
            [   group_id:4, group_name:'SUPER_USER',
                group_desc:'super user group',
                application_id:2
            ],
            primaryKey: false
        )        

        insert( 'csm_user_group_role_pg',
            [   user_group_role_pg_id:36,
                group_id:4, role_id:6,
                protection_group_id:10
            ],
            primaryKey: false
        )

        insert( 'csm_user_group_role_pg',
            [   user_group_role_pg_id:37,
                group_id:4, role_id:6,
                protection_group_id:11
            ],
            primaryKey: false
        )

        insert( 'csm_user_group_role_pg',
            [   user_group_role_pg_id:38,
                group_id:4, role_id:6,
                protection_group_id:12
            ],
            primaryKey: false
        )


        insert( 'csm_user_group_role_pg',
            [   user_group_role_pg_id:42,
                group_id:4, role_id:7,
                protection_group_id:13
            ],
            primaryKey: false
        )

        execute("UPDATE csm_user_group_role_pg SET role_id=3 WHERE user_group_role_pg_id=34");

        
    }
}
