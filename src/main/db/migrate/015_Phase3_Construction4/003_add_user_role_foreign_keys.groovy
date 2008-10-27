class AddUserRoleForeignKeys extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute('DELETE FROM user_roles WHERE user_id NOT IN (SELECT id FROM users)')
        execute('ALTER TABLE user_roles ADD CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users')

        execute('DELETE FROM user_role_sites WHERE user_role_id NOT IN (SELECT id FROM user_roles) OR site_id NOT IN (SELECT id FROM sites)')
        execute('ALTER TABLE user_role_sites ADD CONSTRAINT fk_user_role_site_role FOREIGN KEY (user_role_id) REFERENCES user_roles')
        execute('ALTER TABLE user_role_sites ADD CONSTRAINT fk_user_role_site_site FOREIGN KEY (site_id) REFERENCES sites')

        execute('DELETE FROM user_role_study_sites WHERE user_role_id NOT IN (SELECT id FROM user_roles) OR study_site_id NOT IN (SELECT id FROM study_sites)')
        execute('ALTER TABLE user_role_study_sites ADD CONSTRAINT fk_user_role_study_site_role FOREIGN KEY (user_role_id) REFERENCES user_roles')
        execute('ALTER TABLE user_role_study_sites ADD CONSTRAINT fk_user_role_study_site_study FOREIGN KEY (study_site_id) REFERENCES study_sites')
    }

    void down() {
        [
            user_roles:            ['fk_user_role_user'],
            user_role_sites:       ['fk_user_role_site_role', 'fk_user_role_site_site'],
            user_role_study_sites: ['fk_user_role_study_site_role', 'fk_user_role_study_site_study'],
        ].each { table, constraints ->
            constraints.each { cons ->
                execute("ALTER TABLE " + table + " DROP CONSTRAINT " + constraint);
            }
        }
    }
}