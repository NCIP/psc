class DropUserPassword extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        dropColumn("users", "password")
    }

    void down() {
        addColumn("users", "password", "string")
    }
}
