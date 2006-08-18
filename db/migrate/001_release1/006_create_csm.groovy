class CreateCsm extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('oracle')) {
            String fileName = "db/migrate/001_release1/CSMOracleSQL.sql"
            File input = new File(fileName)
            String content = input.text
            execute(content)
        } else {
            // note that the PostgreSQL scripts work for HSQLDB, too
            String fileName = "db/migrate/001_release1/CSMPostgreSQL.sql"
            File input = new File(fileName)
            String content = input.text
            execute(content)
        }
    }
    
    void down() {
        if (databaseMatches('oracle')) {
            String fileName = "db/migrate/001_release1/CSMOracleSQL-drop.sql"
            File input = new File(fileName)
            String content = input.text
            execute(content)
        } else {
            // note that the PostgreSQL scripts work for HSQLDB, too
            File input = new File("db/migrate/001_release1/CSMPostgreSQL-drop.sql")
            execute(input.text)
        }
    }
}

