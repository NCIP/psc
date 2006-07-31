class CreateCsm extends edu.northwestern.bioinformatics.bering.Migration {
     void up() {
        if (databaseMatches('oracle')) {
        } else if (databaseMatches('postgresql')) {
		String fileName = "CSMPostgreSQL.sql" 
		File input = new File(fileName) 
		String content = input.text
		execute(content)
        } else {
        }

    }

    void down() {
    }
}

