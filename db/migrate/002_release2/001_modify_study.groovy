class ModifyStudy extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
    	execute('ALTER TABLE studies ADD completed Boolean')
    }

    void down() {
 	  	execute('ALTER TABLE studies DROP COLUMN completed')
     }
}
