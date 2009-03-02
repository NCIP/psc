// For some reason user_role_study_sites.study_site_id is a string column instead of an integer
// this makes it impossible to put the appropriate FK on it.
class CorrectUserRoleStudySites extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("user_role_study_sites", "good_study_site_id", "integer")
        if (databaseMatches("postgresql")) {
            // postgresql requires explicit cast
            execute("UPDATE user_role_study_sites SET good_study_site_id=cast(study_site_id as int)")
        } else {
            execute("UPDATE user_role_study_sites SET good_study_site_id=study_site_id")
        }
        removeColumn("user_role_study_sites", "study_site_id")
        renameColumn("user_role_study_sites", "good_study_site_id", "study_site_id")
        setNullable("user_role_study_sites", "study_site_id", false)
    }

    void down() {
        // no point in reversing
    }
}