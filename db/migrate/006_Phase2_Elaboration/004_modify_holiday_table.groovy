class ModifyHolidayTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("holidays", "number_of_week", "integer")
    }

    void down() {
        dropColumn("holidays", "number_of_week");
    }
}