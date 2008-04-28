// KEY is a reserved word on MS-SQL, MySQL, and probably others.
// We shouldn't use it as a column name.  Instead, we'll use "prop",
// short for property (which is also a reserved word in some systems).
class KeyIsAReservedWord extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('sqlserver')) {
            renameColumn("configuration", "key_ms", "prop")
            renameColumn("authentication_system_conf", "key_ms", "prop")
        } else {
            renameColumn("configuration", "key", "prop")
            renameColumn("authentication_system_conf", "key", "prop")
        }
    }

    void down() {
        if (databaseMatches('sqlserver')) {
            renameColumn("authentication_system_conf", "prop", "key_ms")
            renameColumn("configuration", "prop", "key_ms")
        } else {
            renameColumn("authentication_system_conf", "prop", "key")
            renameColumn("configuration", "prop", "key")
        }
    }
}