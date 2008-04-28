// XXX: As written, this migration cannot be taken down and then back up
class CreateCsmForUpt extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('oracle')) {
            external("../001_release1/CSMOracleSQL-drop.sql")
            external("CsmUptOracleSQL_create.sql")
        } else if (databaseMatches('postgresql')){
            external("../001_release1/CSMPostgreSQL-drop.sql")
            external("CsmUptPostgreSQL_create.sql")
        } else {
            external("../001_release1/CSMPostgreSQL-drop.sql")
            external("CsmUptHsqldbSQL_create.sql")
        }
    }
    
    void down() {
        if (databaseMatches('oracle')) {
            external("CsmUptOracleSQL_drop.sql")
            external("../001_release1/CSMOracleSQL.sql")
        } else if (databaseMatches('postgresql')) {
            external("CsmUptPostgreSQL_drop.sql")
            external("../001_release1/CSMPostgreSQL.sql")
        } else {
            external("CsmUptHsqldbSQL_drop.sql")
            external("../001_release1/CSMPostgreSQL.sql")
        }
    }
}

