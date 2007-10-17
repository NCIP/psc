class RemoveOldFlags extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        dropColumn("studies", "amended")
        dropColumn("planned_calendars", "complete")
    }

    void down() {
        addColumn("planned_calendars", "complete", "boolean", nullable: false, defaultValue:"0")
        addColumn("studies", "amended", "boolean", nullable: false, defaultValue:"0")
    }
}
