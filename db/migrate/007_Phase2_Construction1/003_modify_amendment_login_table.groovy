class ModifyAmendmentLoginTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameTable("amendment_logins", "amendments")

        addColumn("amendments", "previous_amendment", "integer")
        addColumn("amendments", "name", "string")

        dropColumn("amendments", "amendment_number")

    }

    void down() {
        dropColumn("amendments", "previous_amendment")
        dropColumn("amendments", "name")

        renameTable("amendments", "amendment_logins")

        addColumn("amendment_logins", "amendment_number", "integer")
    }
}
