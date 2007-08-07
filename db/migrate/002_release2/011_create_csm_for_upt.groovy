// XXX: As written, this migration cannot be taken down and then back up
class CreateCsmForUpt extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('oracle')) {
            File drop = new File("db/migrate/001_release1/CSMOracleSQL-drop.sql")
            execute(drop.text)
            String fileName = "db/migrate/002_release2/CsmUptOracleSQL_create.sql"
            File input = new File(fileName)
            String content = input.text
            execute(content)
        } else if (databaseMatches('postgresql')){
            File drop = new File("db/migrate/001_release1/CSMPostgreSQL-drop.sql")
            execute(drop.text)
            String fileName = "db/migrate/002_release2/CsmUptPostgreSQL_create.sql"
            File input = new File(fileName)
            String content = input.text
            execute(content)
        } else {
            File drop = new File("db/migrate/001_release1/CSMPostgreSQL-drop.sql")
            execute(drop.text)
            String fileName = "db/migrate/002_release2/CsmUptHsqldbSQL_create.sql"
            File input = new File(fileName)
            String content = input.text
            execute(content)
        }
    }
    
    void down() {
        if (databaseMatches('oracle')) {
            String fileName = "db/migrate/002_release2/CsmUptOracleSQL_drop.sql"
            File input = new File(fileName)
            String content = input.text
            execute(content)
            File restore = new File("db/migrate/001_release1/CSMOracleSQL.sql")
            execute(restore.text)
        } else if (databaseMatches('postgresql')) {
            File input = new File("db/migrate/002_release2/CsmUptPostgreSQL_drop.sql")
            execute(input.text)
            File restore = new File("db/migrate/001_release1/CSMPostgreSQL.sql")
            execute(restore.text)
        } else {
            File input = new File("db/migrate/002_release2/CsmUptHsqldbSQL_drop.sql")
            execute(input.text)
            File restore = new File("db/migrate/001_release1/CSMPostgreSQL.sql")
            execute(restore.text)
        }
    }
}

