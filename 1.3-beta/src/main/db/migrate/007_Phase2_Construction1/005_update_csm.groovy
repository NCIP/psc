class UpdateCsm extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {

        // Clean the Study calendar CSM data
        execute("DELETE FROM csm_user_pe")
        execute("DELETE FROM csm_user_group_role_pg")
        execute("DELETE FROM csm_user_group")
        execute("DELETE FROM csm_user")
        execute("DELETE FROM csm_role_privilege")
        execute("DELETE FROM csm_role")
        execute("DELETE FROM csm_protection_group")
        execute("DELETE FROM csm_protection_element")
        execute("DELETE FROM csm_privilege")
        execute("DELETE FROM csm_group")
        execute("DELETE FROM csm_application")

        // Mke sure to delete study calendar domain users to keep in sync with the csm database
        execute("DELETE FROM users")
        execute("DELETE FROM user_roles")

        // Build the Study calendar CSM data
        insert( 'csm_application',
            [   application_id:1, application_name:'csm_upt',
                application_description:'UPT Super Admin Application', declarative_flag:0,
                active_flag:0
            ],
            primaryKey: false
        )

        insert( 'csm_application',
            [   application_id:2, application_name:'study_calendar',
                application_description:'study calendar',declarative_flag:0,
                active_flag:1
            ],
            primaryKey: false
        )


        insert( 'csm_group',
            [   group_id:1, group_name:'STUDY_COORDINATOR',
                group_desc:'study coordinator group',
                application_id:2
            ],
            primaryKey: false
        )

        insert( 'csm_group',
            [   group_id:2, group_name:'STUDY_ADMIN',
                group_desc:'study administrators group',
                application_id:2
            ],
            primaryKey: false
        )

        insert( 'csm_group',
            [   group_id:3, group_name:'PARTICIPANT_COORDINATOR',
                group_desc:'participant coordinator group',
                application_id:2
            ],
            primaryKey: false
        )

        insert( 'csm_group',
            [   group_id:4, group_name:'SUPER_USER',
                group_desc:'super user group',
                application_id:2
            ],
            primaryKey: false
        )

        insert( 'csm_group',
            [   group_id:5, group_name:'RESEARCH_ASSOCIATE',
                group_desc:'research associate group',
                application_id:2
            ],
            primaryKey: false
        )

        insert( 'csm_group',
            [   group_id:6, group_name:'SITE_COORDINATOR',
                group_desc:'site coordinator group',
                application_id:2
            ],
            primaryKey: false
        )


        insert( 'csm_privilege',
            [   privilege_id:1, privilege_name:'CREATE',
                privilege_description:'This privilege grants permission to a user to create an entity. This entity can be an object, a database entry, or a resource such as a network connection'
            ],
            primaryKey: false
        )

        insert( 'csm_privilege',
            [   privilege_id:2, privilege_name:'ACCESS',
                privilege_description:'This privilege allows a user to access a particular resource.  Examples of resources include a network or database connection, socket, module of the application, or even the application itself'
            ],
            primaryKey: false
        )

        insert( 'csm_privilege',
            [   privilege_id:3, privilege_name:'READ',
                privilege_description:'This privilege permits the user to read data from a file, URL, database, an object, etc. This can be used at an entity level signifying that the user is allowed to read data about a particular entry'
            ],
            primaryKey: false
        )

       insert( 'csm_privilege',
            [   privilege_id:4, privilege_name:'WRITE',
                privilege_description:'This privilege permits the user to read data from a file, URL, database, an object, etc. This can be used at an entity level signifying that the user is allowed to read data about a particular entry'
            ],
            primaryKey: false
        )

        insert( 'csm_privilege',
            [   privilege_id:5, privilege_name:'UPDATE',
                privilege_description:'This privilege grants permission at an entity level and signifies that the user is allowed to update data for a particular entity. Entities may include an object, object attribute, database row etc'
            ],
            primaryKey: false
        )

        insert( 'csm_privilege',
            [   privilege_id:6, privilege_name:'DELETE',
                privilege_description:'This privilege permits a user to delete a logical entity. This entity can be an object, a database entry, a resource such as a network connection, etc'
            ],
            primaryKey: false
        )

        insert( 'csm_privilege',
            [   privilege_id:7, privilege_name:'EXECUTE',
                privilege_description:'This privilege allows a user to execute a particular resource. The resource can be a method, function, behavior of the application, URL, button etc'
            ],
            primaryKey: false
        )

        insert( 'csm_protection_element',
            [   protection_element_id:1, protection_element_name:'csm_upt',
                protection_element_description:'UPT Super Admin Application', object_id:'csm_upt',
                application_id:1
            ],
            primaryKey: false
        )

        insert( 'csm_protection_element',
            [   protection_element_id:2, protection_element_name:'study_calendar',
                object_id:'study_calendar', application_id:1
            ],
            primaryKey: false
        )

        insert( 'csm_protection_group',
            [   protection_group_id:9, protection_group_name:'BaseAccess',
                protection_group_description:'group of features which all users have access to', application_id:2,
                large_element_count_flag:0
            ],
            primaryKey: false
        )

        insert( 'csm_protection_group',
            [   protection_group_id:10, protection_group_name:'CreateStudyAccess',
                protection_group_description:'access to create study, epochs, arms and associate periods to arms', application_id:2,
                large_element_count_flag:0
            ],
            primaryKey: false
        )

         insert( 'csm_protection_group',
            [   protection_group_id:11, protection_group_name:'AdministrativeAccess',
                protection_group_description:'access to mark template complete', application_id:2,
                large_element_count_flag:0
            ],
            primaryKey: false
        )

        insert( 'csm_protection_group',
            [   protection_group_id:12, protection_group_name:'ParticipantAssignmentAccess',
                protection_group_description:'access to create and assign participants', application_id:2,
                large_element_count_flag:0
            ],
            primaryKey: false
        )

        insert( 'csm_protection_group',
            [   protection_group_id:13, protection_group_name:'SiteCoordinatorAccess',
                protection_group_description:'access for site coordinators', application_id:2,
                large_element_count_flag:0
            ],
            primaryKey: false
        )

        insert( 'csm_protection_group',
            [   protection_group_id:15, protection_group_name:'BaseSitePG',
                protection_group_description:'Base site protection group', application_id:2,
                large_element_count_flag:0
            ],
            primaryKey: false
        )

        insert( 'csm_role',
            [   role_id:2, role_name:'STUDY_ADMIN',
                role_description:'study administrator', application_id:2,
                active_flag:1
            ],
            primaryKey: false
        )

        insert( 'csm_role',
            [   role_id:3, role_name:'STUDY_COORDINATOR',
                role_description:'study coordinator', application_id:2,
                active_flag:1
            ],
            primaryKey: false
        )

        insert( 'csm_role',
            [   role_id:4, role_name:'PARTICIPANT_COORDINATOR',
                role_description:'participant coordinator', application_id:2,
                active_flag:1
            ],
            primaryKey: false
        )

        insert( 'csm_role',
            [   role_id:5, role_name:'RESEARCH_ASSOCIATE',
                role_description:'clinical research associate', application_id:2,
                active_flag:1
            ],
            primaryKey: false
        )

        insert( 'csm_role',
            [   role_id:6, role_name:'SUPERUSER',
                role_description:'super user for this application for testing purposes', application_id:2,
                active_flag:1
            ],
            primaryKey: false
        )

        insert( 'csm_role',
            [   role_id:7, role_name:'SITE_COORDINATOR',
                role_description:'site coordinator role', application_id:2,
                active_flag:1
            ],
            primaryKey: false
        )


        insert( 'csm_role_privilege',
            [   role_privilege_id:7, role_id:2,
                privilege_id:2
            ],
            primaryKey: false
        )

        insert( 'csm_role_privilege',
            [   role_privilege_id:9, role_id:3,
                privilege_id:2
            ],
            primaryKey: false
        )

        insert( 'csm_role_privilege',
            [   role_privilege_id:10, role_id:4,
                privilege_id:2
            ],
            primaryKey: false
        )

        insert( 'csm_role_privilege',
            [   role_privilege_id:11, role_id:5,
                privilege_id:2
            ],
            primaryKey: false
        )

        insert( 'csm_role_privilege',
            [   role_privilege_id:12, role_id:6,
                privilege_id:7
            ],
            primaryKey: false
        )

        insert( 'csm_role_privilege',
            [   role_privilege_id:13, role_id:6,
                privilege_id:6
            ],
            primaryKey: false
        )

        insert( 'csm_role_privilege',
            [   role_privilege_id:14, role_id:6,
                privilege_id:5
            ],
            primaryKey: false
        )

        insert( 'csm_role_privilege',
            [   role_privilege_id:15, role_id:6,
                privilege_id:3
            ],
            primaryKey: false
        )

        insert( 'csm_role_privilege',
            [   role_privilege_id:16, role_id:6,
                privilege_id:2
            ],
            primaryKey: false
        )

        insert( 'csm_role_privilege',
            [   role_privilege_id:17, role_id:6,
                privilege_id:4
            ],
            primaryKey: false
        )

        insert( 'csm_role_privilege',
            [   role_privilege_id:18, role_id:6,
                privilege_id:1
            ],
            primaryKey: false
        )

         insert( 'csm_role_privilege',
            [   role_privilege_id:19, role_id:7,
                privilege_id:2
            ],
            primaryKey: false
        )


        insert( 'csm_user_group_role_pg',
            [   user_group_role_pg_id:29,
                group_id:1, role_id:3,
                protection_group_id:9
            ],
            primaryKey: false
        )

        insert( 'csm_user_group_role_pg',
            [   user_group_role_pg_id:30,
                group_id:1, role_id:3,
                protection_group_id:10
            ],
            primaryKey: false
        )

        insert( 'csm_user_group_role_pg',
            [   user_group_role_pg_id:31,
                group_id:2, role_id:2,
                protection_group_id:9
            ],
            primaryKey: false
        )

        insert( 'csm_user_group_role_pg',
            [   user_group_role_pg_id:32,
                group_id:2, role_id:2,
                protection_group_id:11
            ],
            primaryKey: false
        )

        insert( 'csm_user_group_role_pg',
            [   user_group_role_pg_id:33,
                group_id:3, role_id:4,
                protection_group_id:9
            ],
            primaryKey: false
        )

        insert( 'csm_user_group_role_pg',
            [   user_group_role_pg_id:34,
                group_id:3, role_id:3,
                protection_group_id:12
            ],
            primaryKey: false
        )

        insert( 'csm_user_group_role_pg',
            [   user_group_role_pg_id:35,
                group_id:4, role_id:6,
                protection_group_id:9
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
            [   user_group_role_pg_id:39,
                group_id:5, role_id:5,
                protection_group_id:9
            ],
            primaryKey: false
        )

        insert( 'csm_user_group_role_pg',
            [   user_group_role_pg_id:40,
                group_id:6, role_id:7,
                protection_group_id:9
            ],
            primaryKey: false
        )

        insert( 'csm_user_group_role_pg',
            [   user_group_role_pg_id:41,
                group_id:6, role_id:7,
                protection_group_id:13
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

        
        
    }

     void down() {}
}