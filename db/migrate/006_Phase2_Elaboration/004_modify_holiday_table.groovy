class ModifyHolidayTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute('ALTER TABLE holidays ADD COLUMN number_of_week integer')

    }

    void down() {
        execute('ALTER TABLE holidays DROP COLUMN number_of_week')
    }
}