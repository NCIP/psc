class ModifyLoginAudits extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('oracle')) {
            execute('ALTER TABLE login_audits MODIFY time timestamp')
        } else if (databaseMatches('postgresql')) {
            execute('ALTER TABLE login_audits ALTER COLUMN time TYPE timestamp')
        } else {
		execute('ALTER TABLE login_audits ALTER COLUMN time timestamp')
	  }
    }

    void down() {
       if (databaseMatches('oracle')) {
            execute('ALTER TABLE login_audits MODIFY time date')
        } else if (databaseMatches('postgresql')) {
            execute('ALTER TABLE login_audits ALTER COLUMN time TYPE date')
        } else {
		execute('ALTER TABLE login_audits ALTER COLUMN time date')
	  }

    }
}
