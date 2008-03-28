class CreateHolidayTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("holidays") { t ->
            t.addVersionColumn()
            t.addColumn("discriminator_id", "integer", nullable:false)
            t.addColumn("site_Id", "integer", nullable: false)
            t.addColumn("day", "integer", nullable: true)
            t.addColumn("month", "integer", nullable: true)
            t.addColumn("year", "integer", nullable: true)
            t.addColumn("day_of_the_week", "string", nullable: true)
            t.addColumn("status", "string", nullable: false)
        }
    }

    void down() {
        dropTable("holidays");
    }
}