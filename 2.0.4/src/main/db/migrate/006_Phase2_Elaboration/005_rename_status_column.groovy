class RenameStatusColumn extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("holidays", "week_number", "integer");
        execute('UPDATE holidays SET week_number=number_of_week')
        dropColumn("holidays", "number_of_week");
        renameColumn("holidays", "status", "description");
    }

    void down() {
        addColumn("holidays", "number_of_week", "integer");
        execute('UPDATE holidays SET number_of_week=week_number')
        dropColumn("holidays", "week_number");
        renameColumn("holidays", "description", "status");
    }
}