class RemoveOldProtectionGroups extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("UPDATE csm_protection_group SET parent_protection_group_id=null")
        execute("DELETE FROM csm_protection_group where protection_group_id>=9 AND protection_group_id<=15")
    }

    void down() {
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
    }
}
