class CreateCsm extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('oracle')) {
            external("CSMOracleSQL.sql")
        } else {
            // note that the PostgreSQL scripts work for HSQLDB, too
            external("CSMPostgreSQL.sql")
        }
    }
    
    void down() {
        if (databaseMatches('oracle')) {
            external("CSMOracleSQL-drop.sql")
        } else {
            // note that the PostgreSQL scripts work for HSQLDB, too
            external("CSMPostgreSQL-drop.sql")
        }
    }
}

