class UpdateCsmGroup extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
       insert( 'csm_group',
            [   group_id:7, group_name:'SYSTEM_ADMINISTRATOR',
                group_desc:'system administrator group',
                application_id:2
            ],
            primaryKey: false
        )
    }


    void down() {
    }
}