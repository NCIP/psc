class ManagingSitesShouldNotHavePk extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        dropColumn('managing_sites', 'id')
    }

    void down() {
        execute("DELETE FROM managing_sites")
        addColumn('managing_sites', 'id', 'integer', primaryKey: true)
    }
}
