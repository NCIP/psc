class RemoveNameColumnFromStudiesTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("UPDATE studies SET assigned_identifier=name")
        dropColumn("studies", "name")
        
    }

    void down() {
        addColumn("studies", "name", "string", defaultValue:"", nullable: false)
        execute("UPDATE studies SET name=assigned_identifier")
        execute("UPDATE studies SET assigned_identifier='' ")
    }

}

