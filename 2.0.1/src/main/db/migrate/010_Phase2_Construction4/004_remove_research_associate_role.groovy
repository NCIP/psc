class RemoveResearchAssociateRole extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("delete from csm_role where role_id=5");
        execute("delete from csm_group where group_id=5");
        execute("delete from csm_user_group_role_pg where role_id=5");
		execute("delete from csm_role_privilege where role_id=5");
		execute("delete from csm_user_group where group_id=5");
		execute("delete from user_roles where csm_group_name='RESEARCH_ASSOCIATE'");
    }

    void down() {

           insert( 'csm_role',
            [   role_id:5, role_name:'RESEARCH_ASSOCIATE',
                role_description:'clinical research associate', application_id:2,
                active_flag:1
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


    }
}