class ModifyStudy extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
 		if (databaseMatches('oracle')) {
     		execute('ALTER TABLE studies ADD completed integer NOT NULL')
     	} else if (databaseMatches('postgresql')) {
     		execute('ALTER TABLE studies ADD completed Boolean NOT NULL')     	
     	} else if (databaseMatches('hsqldb')) {
     		execute('ALTER TABLE studies ADD completed BOOLEAN NOT NULL')     	
     	}
     		
    }

    void down() {
 	  	execute('ALTER TABLE studies DROP COLUMN completed')
     }
}
