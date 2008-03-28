class InsertSecurityData extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
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

     void down() {
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
    }
}