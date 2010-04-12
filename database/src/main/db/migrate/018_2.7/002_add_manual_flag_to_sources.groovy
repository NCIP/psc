class AddManualFlagToSources extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("sources", "manual_flag", "boolean");
        execute("ALTER TABLE sources ADD CONSTRAINT un_one_manual_source UNIQUE (manual_flag)");
        if (databaseMatches("oracle")) {
            execute("UPDATE sources SET manual_flag=1 WHERE name='PSC - Manual Activity Creation'");
        } else {
            execute("UPDATE sources set manual_flag=true WHERE name='PSC - Manual Activity Creation'");
        }
    }

    void down() {
        execute("ALTER TABLE sources DROP CONSTRAINT un_one_manual_source")
        removeColumn("sources", "manual_flag");
    }
}
