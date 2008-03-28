class CreateLoginAudits extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('login_audits') { t ->
          t.addColumn('ip_address', 'string', nullable:false)
	    t.addColumn('login_status', 'string', nullable:false)
	    t.addColumn('time', 'date', nullable:false)
          t.addColumn('user_name', 'string', nullable:false)
 	    t.addColumn('version', 'integer', nullable:false)
        }
    }

    void down() {
        dropTable('login_audits')
    }
}