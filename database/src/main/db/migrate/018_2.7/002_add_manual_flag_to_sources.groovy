class AddManualFlagToSources extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("sources", "manual_flag", "boolean");
        execute("ALTER TABLE sources ADD CONSTRAINT un_one_manual_source UNIQUE (manual_flag)")
        execute("update sources set manual_flag = 'TRUE' where name = 'PSC - Manual Activity Creation' ");
    }

    void down() {
        execute("ALTER TABLE sources DROP CONSTRAINT un_one_manual_source")
        removeColumn("sources", "manual_flag");

    }
}