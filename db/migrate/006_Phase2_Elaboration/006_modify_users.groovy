class ModifyAuditEvents extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("users", "active_flag", "boolean", nullable: false, defaultValue:"0")
    }

    void down() {
        dropColumn("users", "active_flag")
    }
}
