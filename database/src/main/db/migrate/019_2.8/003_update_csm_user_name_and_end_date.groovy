class UpdateCsmUserNameAndEndDate extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('hsqldb') || databaseMatches('oracle')) {
            execute("UPDATE csm_user SET first_name=(SELECT (CASE WHEN u.first_name IS NULL THEN csm_user.first_name ELSE u.first_name END) FROM users u WHERE u.csm_user_id=csm_user.user_id)");
            execute("UPDATE csm_user SET last_name=(SELECT (CASE WHEN u.last_name IS NULL THEN csm_user.last_name ELSE u.last_name END) FROM users u WHERE u.csm_user_id=csm_user.user_id)");

            execute("UPDATE csm_user cu SET end_date = CURRENT_TIMESTAMP WHERE user_id in (SELECT csm_user_id FROM users WHERE active_flag = 0)");
        } else {
            execute("UPDATE csm_user cu1 SET first_name=temp.first_name FROM (SELECT u.csm_user_id, u.first_name FROM users u, csm_user cu WHERE cu.user_id = u.csm_user_id AND (u.first_name is not null AND u.first_name!='')) temp WHERE temp.csm_user_id=cu1.user_id");
            execute("UPDATE csm_user cu1 SET last_name=temp.last_name FROM (SELECT u.csm_user_id, u.last_name FROM users u, csm_user cu WHERE cu.user_id = u.csm_user_id AND (u.last_name is not null AND u.last_name!='')) temp WHERE temp.csm_user_id=cu1.user_id");

            execute("UPDATE csm_user cu SET end_date = CURRENT_TIMESTAMP WHERE user_id in (SELECT csm_user_id FROM users WHERE active_flag = false)");
        }
    }

    void down() {
        execute("update csm_user set first_name='.'")
        execute("update csm_user set last_name='.'")
        execute("update csm_user set end_date = null")
    }
}