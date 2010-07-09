class UpgradeToCsm42 extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        // csm_user table changes
        addColumn('csm_user', 'migrated_flag', 'integer', defaultValue: 0)
        addColumn('csm_user', 'premgrt_login_name', 'string', limit: 100)

        // csm_application table changes
        setDefaultValue('csm_application', 'declarative_flag', '0')
        setDefaultValue('csm_application', 'active_flag', '0')
        addColumn('csm_application', 'csm_version', 'string', limit: 20)

        // defaults for update_date
        // can't use setDefaultValue because bering will quote the value
        if (databaseMatches('oracle')) {
            execute('ALTER TABLE csm_application MODIFY (update_date DEFAULT SYSDATE)')
            execute('ALTER TABLE csm_user        MODIFY (update_date DEFAULT SYSDATE)')
        } else {
            execute('ALTER TABLE csm_application ALTER COLUMN update_date SET DEFAULT current_date')
            execute('ALTER TABLE csm_user        ALTER COLUMN update_date SET DEFAULT current_date')
        }

        if (databaseMatches('hsqldb')) {
            external("001_csm42_up_hsqldb.sql")
        } else if (databaseMatches('oracle')) {
            external("001_csm42_up_oracle.sql")
        } else {
            external("001_csm42_up_postgresql.sql")
        }
    }

    void down() {
        // Down reverts table changes & new/removed objects only -- does not
        // revert the recreated tables to their CSM 3.2 state.
        dropColumn('csm_application', 'csm_version')

        dropColumn('csm_user', 'migrated_flag')
        dropColumn('csm_user', 'premgrt_login_name')

        if (databaseMatches('hsqldb')) {
            // Note: as PSC uses it, it's not possible to run down migrations 
            // on hsqldb. This script should work if that ever changes.
            external("001_csm42_limited_down_hsqldb.sql")
        } else if (databaseMatches('oracle')) {
            external("001_csm42_limited_down_oracle.sql")
        } else {
            external("001_csm42_limited_down_postgresql.sql")
        }
    }
}
