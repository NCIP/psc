class ModifyParticipants extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
	if (databaseMatches('oracle')) {
          execute('ALTER TABLE participants RENAME COLUMN social_security_number TO person_id ')
      } else if (databaseMatches('postgresql') {
          execute('ALTER TABLE participants RENAME COLUMN social_security_number TO person_id ')
    	} else {
	    execute('ALTER TABLE participants ALTER COLUMN social_security_number RENAME TO person_id')
	}
    }

    void down() {
        execute('ALTER TABLE participants DROP COLUMN person_id')
    }
}