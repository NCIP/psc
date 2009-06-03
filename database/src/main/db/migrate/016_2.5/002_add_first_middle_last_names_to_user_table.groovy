class AddFirstMiddleLastNamesToUserTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("users", "first_name", "string", limit: 255)
        addColumn("users", "middle_name", "string", limit: 255)
        addColumn("users", "last_name", "string", limit: 255)
    }

    void down() {
        removeColumn("users", "first_name")
        removeColumn("users", "middle_name")
        removeColumn("users", "last_name")
    }
}