class ModifySites extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("sites", "assigned_identifier", "string", limit: 900);
        execute("update sites set assigned_identifier = name");
    }

    void down() {
         dropColumn("sites","assigned_identifier");
    }
}
