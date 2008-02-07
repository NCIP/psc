class InsertSecurityData extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {

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
    }

     void down() {
        execute("DELETE FROM csm_protection_group where protection_group_id = 17")
        execute("DELETE FROM csm_role where role_id = 8")
        execute("DELETE FROM csm_user_group_role_pg where user_group_role_pg_id = 43")
        execute("DELETE FROM csm_role_privilege where role_privilege_id = 20")
        execute("DELETE FROM csm_protection_element where protection_element_id = 88")
        execute("DELETE FROM csm_pg_pe where pg_pe_id = 90")
    }
}