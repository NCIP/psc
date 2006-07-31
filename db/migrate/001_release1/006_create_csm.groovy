class CreateCsm extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('oracle')) {
        } else if (databaseMatches('postgresql')) {
	    String fileName = "db/migrate/001_release1/CSMPostgreSQL.sql" 
	    File input = new File(fileName) 
	    String content = input.text
	    execute(content)
        } else {
        }
    }
    
    void down() {
    }
}

