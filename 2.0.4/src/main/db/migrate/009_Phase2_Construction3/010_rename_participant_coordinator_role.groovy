class RenameParticipantCoordinatorRole extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("UPDATE csm_role SET role_name='SUBJECT_COORDINATOR' WHERE role_name='PARTICIPANT_COORDINATOR'");
        execute("UPDATE csm_role SET role_description='subject coordinator' WHERE role_description='participant coordinator'");

        execute("UPDATE user_roles SET csm_group_name='SUBJECT_COORDINATOR' WHERE csm_group_name='PARTICIPANT_COORDINATOR'");

        execute("UPDATE csm_group SET group_name='SUBJECT_COORDINATOR' WHERE group_name='PARTICIPANT_COORDINATOR'");
        execute("UPDATE csm_group SET group_desc='subject coordinator group' WHERE group_desc='participant coordinator group'");
    }

    void down() {
        execute("UPDATE csm_role SET role_name='PARTICIPANT_COORDINATOR' WHERE role_name='SUBJECT_COORDINATOR'");
        execute("UPDATE csm_role SET role_description='participant coordinator' WHERE role_description='subject coordinator'");

        execute("UPDATE user_roles SET csm_group_name='PARTICIPANT_COORDINATOR' WHERE csm_group_name='SUBJECT_COORDINATOR'");

        execute("UPDATE csm_group SET group_name='PARTICIPANT_COORDINATOR' WHERE group_name='SUBJECT_COORDINATOR'");
        execute("UPDATE csm_group SET group_desc='participant coordinator group' WHERE group_desc='subject coordinator group'");
    }
}