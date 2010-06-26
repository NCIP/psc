class UnifiedAuthData extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('oracle')) {
            external("002_unified_auth_seed_oracle.sql")
        } else if (databaseMatches('hsqldb')) {
            external("002_unified_auth_seed_hsqldb.sql")
        } else {
            external("002_unified_auth_seed_postgresql.sql")
        }
    }

    void down() {
        execute("DELETE FROM csm_role_privilege")
        execute("DELETE FROM csm_privilege")
        execute("DELETE FROM csm_role")
        execute("DELETE FROM csm_group")
    }
}
