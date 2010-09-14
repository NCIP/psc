class AddActivitySourceForeignKeys extends edu.northwestern.bioinformatics.bering.Migration {

    void up() {
        if (databaseMatches("oracle")) {
            execute("UPDATE activities a SET source_id=(SELECT id FROM sources WHERE manual_flag=1)
                        WHERE ((a.source_id IS NULL) OR (a.source_id NOT IN (SELECT id FROM sources))) AND a.name!='Reconsent'");
        } else {
            execute("UPDATE activities a SET source_id=(SELECT id FROM sources WHERE manual_flag=true)
                        WHERE ((a.source_id IS NULL) OR (a.source_id NOT IN (SELECT id FROM sources))) AND a.name!='Reconsent'");
        }
        execute("ALTER TABLE activities ADD CONSTRAINT fk_source_id FOREIGN KEY (source_id) REFERENCES sources");
    }

    void down() {
        execute("ALTER TABLE activities DROP CONSTRAINT fk_source_id");
    }
}