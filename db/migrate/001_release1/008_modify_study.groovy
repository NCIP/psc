class ModifyStudy extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
 		if (databaseMatches('oracle')) {
     		execute('ALTER TABLE studies ADD completed integer')
     	} else if (databaseMatches('postgresql')) {
     		execute('ALTER TABLE studies ADD completed Boolean')     	
     	} else if (databaseMatches('hsqldb')) {
     		execute('ALTER TABLE studies ADD completed BOOLEAN')     	
     	}
     		
    }

    void down() {
 	  	execute('ALTER TABLE studies DROP COLUMN completed')
     }
}
