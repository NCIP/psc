class AddLoadStatusAndLongTitle extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("studies", "load_status", "integer", nullable: true, defaultValue:"1");

        execute("UPDATE studies SET load_status = 1");

        setNullable("studies", "load_status", false);

        addColumn("studies", "long_title", "string", nullable: true);

        renameColumn('studies', 'protocol_authority_id', 'assigned_identifier')
    }

    void down() {
        renameColumn('studies', 'assigned_identifier', 'protocol_authority_id')

        dropColumn("studies","long_title")
        dropColumn("studies","load_status")
    }
}

