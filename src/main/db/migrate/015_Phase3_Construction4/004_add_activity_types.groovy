class AddActivityTypes extends edu.northwestern.bioinformatics.bering.Migration {
	void up() {
	    addColumn("activity_types", "grid_id", "string", limit: 255)

        execute("ALTER TABLE activity_types ADD CONSTRAINT un_activity_types_grid_id UNIQUE (grid_id)")
		execute("insert into activity_types (id, version, name, grid_id) values ('1', '0', 'Disease Measure', '1')");
		execute("insert into activity_types (id, version, name, grid_id) values ('2', '0', 'Intervention', '2')");
		execute("insert into activity_types (id, version, name, grid_id) values ('3', '0', 'Lab Test', '3')");
		execute("insert into activity_types (id, version, name, grid_id) values ('4', '0', 'Procedure', '4')");
		execute("insert into activity_types (id, version, name, grid_id) values ('5', '0', 'Other', '5')");


        if (databaseMatches('oracle')) {
            execute("""\
                alter sequence seq_activity_types_id increment by 4;
                select seq_activity_types_id.nextval from dual;
                alter sequence seq_activity_types_id increment by 1;""");
        } else if (databaseMatches('postgresql')){
            execute("SELECT setval('activity_types_id_seq', 5)");
        }

        execute("ALTER TABLE activities ADD CONSTRAINT fk_activ_type_id FOREIGN KEY (activity_type_id) REFERENCES activity_types")
	}

	void down() {
	    execute("ALTER TABLE activities DROP CONSTRAINT fk_activ_type_id")
        if (databaseMatches('oracle')) {
            execute("""\
                alter sequence seq_activity_types_id increment by -5;
                select seq_activity_types_id.nextval from dual;""");
        } else if (databaseMatches('postgresql')){
            execute("SELECT setval('activity_types_id_seq', 1)");
        }
	    execute("alter table activity_types DROP CONSTRAINT un_activity_types_grid_id")
	    removeColumn("activity_types", "grid_id")
		execute("delete from activity_types");
	}
}