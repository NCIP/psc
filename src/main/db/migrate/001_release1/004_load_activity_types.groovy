class LoadActivityTypes extends edu.northwestern.bioinformatics.bering.Migration {
	void up() {
		execute("insert into activity_types (id, version, name) values ('1', '0', 'therapeutic/interventional')");
		execute("insert into activity_types (id, version, name) values ('2', '0', 'observational')");
		execute("insert into activity_types (id, version, name) values ('3', '0', 'correlative')");
		execute("insert into activity_types (id, version, name) values ('4', '0', 'ancillary')");
		execute("insert into activity_types (id, version, name) values ('5', '0', 'prevention')");
		execute("insert into activity_types (id, version, name) values ('6', '0', 'screening')");
		execute("insert into activity_types (id, version, name) values ('7', '0', 'early detection')");
		execute("insert into activity_types (id, version, name) values ('8', '0', 'supportive care')");
		execute("insert into activity_types (id, version, name) values ('9', '0', 'epidemiologic')");
		execute("insert into activity_types (id, version, name) values ('10', '0', 'biobanking')");
	}
	
	void down() {
		execute("delete from activity_types");
	}
}
