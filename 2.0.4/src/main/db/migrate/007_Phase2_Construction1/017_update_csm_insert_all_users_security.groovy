class UpdateCsmInsertAllSecurity extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {

        // ACCESS
        execute("DELETE FROM csm_role where role_id = 9")
        execute("DELETE FROM csm_user_group_role_pg where user_group_role_pg_id = 44")
        execute("DELETE FROM csm_role_privilege where role_privilege_id = 21")

        // CREATE
        execute("DELETE FROM csm_role where role_id = 10")
        execute("DELETE FROM csm_user_group_role_pg where user_group_role_pg_id = 45")
        execute("DELETE FROM csm_role_privilege where role_privilege_id = 22")

        // WRITE
        execute("DELETE FROM csm_role where role_id = 11")
        execute("DELETE FROM csm_user_group_role_pg where user_group_role_pg_id = 46")
        execute("DELETE FROM csm_role_privilege where role_privilege_id = 23")

        // UPDATE
        execute("DELETE FROM csm_role where role_id = 12")
        execute("DELETE FROM csm_user_group_role_pg where user_group_role_pg_id = 47")
        execute("DELETE FROM csm_role_privilege where role_privilege_id = 23")

        // DELETE
        execute("DELETE FROM csm_role where role_id = 13")
        execute("DELETE FROM csm_user_group_role_pg where user_group_role_pg_id = 48")
        execute("DELETE FROM csm_role_privilege where role_privilege_id = 24")

        // EXECUTE
        execute("DELETE FROM csm_role where role_id = 14")
        execute("DELETE FROM csm_user_group_role_pg where user_group_role_pg_id = 49")
        execute("DELETE FROM csm_role_privilege where role_privilege_id = 25")

        // GENERAL
        execute("DELETE FROM csm_protection_group where protection_group_id = 17")
        execute("DELETE FROM csm_role where role_id = 8")
        execute("DELETE FROM csm_user_group_role_pg where user_group_role_pg_id = 43")
        execute("DELETE FROM csm_role_privilege where role_privilege_id = 20")
        execute("DELETE FROM csm_protection_element where protection_element_id = 88")
        execute("DELETE FROM csm_pg_pe where pg_pe_id = 90")

        /*// Setup Protection Elements (Bean Post Processor?) csm_protection_element
        insert(
            'csm_protection_element',
            [
                protection_element_id: 88,
                protection_element_name: '/pages/admin/configure',
                object_id: '/pages/admin/configure',
                attribute: '',
                application_id: 2
            ],
            primaryKey: false
        )

        // Assign Protection Elements to Protection Groups (Bean Post Processor?) csm_pg_pe
        insert(
            'csm_pg_pe',
            [
                pg_pe_id: 90,
                protection_group_id: 11,
                protection_element_id: 88
            ],
            primaryKey: false
        ) */
        

        

    }

    void down() {
        insert(
            'csm_protection_group',
            [
                protection_group_id: 17,
                protection_group_name: 'edu.northwestern.bioinformatics.studycalendar.domain.Study',
                protection_group_description: 'Study Protection Group',
                application_id: 2, large_element_count_flag: 0
            ],
            primaryKey: false
        )


        insert(
            'csm_role',
            [
                role_id: 8, role_name: 'edu.northwestern.bioinformatics.studycalendar.domain.Study.READ',
                role_description: '', application_id: 2, active_flag: 1
            ],
            primaryKey: false
        )

        insert(
            'csm_user_group_role_pg',
            [
                user_group_role_pg_id: 43,
                group_id: 4, role_id: 8, protection_group_id: 17
            ],
            primaryKey: false
        )


        insert(
            'csm_role_privilege',
            [
                role_privilege_id: 20, role_id: 8, privilege_id: 3
            ],
            primaryKey: false
        )

        insert(
            'csm_protection_element',
            [
                protection_element_id: 88,
                protection_element_name: 'edu.northwestern.bioinformatics.studycalendar.domain.Study',
                protection_element_description: 'Study Protection Element',
                object_id: 'edu.northwestern.bioinformatics.studycalendar.domain.Study',
                attribute: '',
                application_id: 2
            ],
            primaryKey: false
        )

        insert(
            'csm_pg_pe',
            [
                pg_pe_id: 90,
                protection_group_id: 17,
                protection_element_id: 88
            ],
            primaryKey: false
        )

         // ACCESS
        insert(
            'csm_role',
            [
                role_id: 9, role_name: 'edu.northwestern.bioinformatics.studycalendar.domain.Study.ACCESS',
                role_description: '', application_id: 2, active_flag: 1
            ],
            primaryKey: false
        )

        insert(
            'csm_user_group_role_pg',
            [
                user_group_role_pg_id: 44,
                group_id: 4, role_id: 9, protection_group_id: 17
            ],
            primaryKey: false
        )

        insert(
            'csm_role_privilege',
            [
                role_privilege_id: 21, role_id: 9, privilege_id: 2
            ],
            primaryKey: false
        )


        // CREATE
        insert(
            'csm_role',
            [
                role_id: 10, role_name: 'edu.northwestern.bioinformatics.studycalendar.domain.Study.CREATE',
                role_description: '', application_id: 2, active_flag: 1
            ],
            primaryKey: false
        )

        insert(
            'csm_user_group_role_pg',
            [
                user_group_role_pg_id: 45,
                group_id: 4, role_id: 10, protection_group_id: 17
            ],
            primaryKey: false
        )

        insert(
            'csm_role_privilege',
            [
                role_privilege_id: 22, role_id: 10, privilege_id: 1
            ],
            primaryKey: false
        )

        // WRITE
        insert(
            'csm_role',
            [
                role_id: 11, role_name: 'edu.northwestern.bioinformatics.studycalendar.domain.Study.WRITE',
                role_description: '', application_id: 2, active_flag: 1
            ],
            primaryKey: false
        )

        insert(
            'csm_user_group_role_pg',
            [
                user_group_role_pg_id: 46,
                group_id: 4, role_id: 11, protection_group_id: 17
            ],
            primaryKey: false
        )

        insert(
            'csm_role_privilege',
            [
                role_privilege_id: 23, role_id: 11, privilege_id: 4
            ],
            primaryKey: false
        )

        // UPDATE
        insert(
            'csm_role',
            [
                role_id: 12, role_name: 'edu.northwestern.bioinformatics.studycalendar.domain.Study.UPDATE',
                role_description: '', application_id: 2, active_flag: 1
            ],
            primaryKey: false
        )

        insert(
            'csm_user_group_role_pg',
            [
                user_group_role_pg_id: 47,
                group_id: 4, role_id: 12, protection_group_id: 17
            ],
            primaryKey: false
        )

        insert(
            'csm_role_privilege',
            [
                role_privilege_id: 24, role_id: 12, privilege_id: 5
            ],
            primaryKey: false
        )

        // DELETE
        insert(
            'csm_role',
            [
                role_id: 13, role_name: 'edu.northwestern.bioinformatics.studycalendar.domain.Study.DELETE',
                role_description: '', application_id: 2, active_flag: 1
            ],
            primaryKey: false
        )

        insert(
            'csm_user_group_role_pg',
            [
                user_group_role_pg_id: 48,
                group_id: 4, role_id: 13, protection_group_id: 17
            ],
            primaryKey: false
        )

        insert(
            'csm_role_privilege',
            [
                role_privilege_id: 25, role_id: 13, privilege_id: 6
            ],
            primaryKey: false
        )

        // EXECUTE
        insert(
            'csm_role',
            [
                role_id: 14, role_name: 'edu.northwestern.bioinformatics.studycalendar.domain.Study.EXECUTE',
                role_description: '', application_id: 2, active_flag: 1
            ],
            primaryKey: false
        )

        insert(
            'csm_user_group_role_pg',
            [
                user_group_role_pg_id: 49,
                group_id: 4, role_id: 14, protection_group_id: 17
            ],
            primaryKey: false
        )

        insert(
            'csm_role_privilege',
            [
                role_privilege_id: 26, role_id: 14, privilege_id: 7
            ],
            primaryKey: false
        )
         
    }
}
