class CreateCsm extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('oracle')) {
            String fileName = "db/migrate/001_release1/CSMOracleSQL.sql"
            File input = new File(fileName)
            String content = input.text
            execute(content)
        } else if (databaseMatches('postgresql')) {
            String fileName = "db/migrate/001_release1/CSMPostgreSQL.sql"
            File input = new File(fileName)
            String content = input.text
            execute(content)
        } else {
            // TODO: need something for HSQLDB.  PostgreSQL script will probably work.
        }
    }
    
    void down() {
        if (databaseMatches('oracle')) {
            String fileName = "db/migrate/001_release1/CSMOracleSQL-drop.sql"
            File input = new File(fileName)
            String content = input.text
            execute(content)
        } else if (databaseMatches('postgresql')) {
            File input = new File("db/migrate/001_release1/CSMPostgreSQL-drop.sql")
            execute(input.text)
        } else {
            // TODO: need something for HSQLDB.  PostgreSQL script will probably work.
        }
    }
}

