class ModifyAuditEvents extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("users", "password", "string", nullable: false, defaultValue:"")
    }

    void down() {
        dropColumn("users", "password")
    }
}
 