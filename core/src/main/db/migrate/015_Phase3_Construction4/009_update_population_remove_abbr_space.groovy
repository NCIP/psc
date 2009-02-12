class CreateActivityProperties extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('oracle') || databaseMatches('postgresql') || databaseMatches('sqlserver')) {
            execute("update populations set abbreviation=translate(abbreviation, ' ', '')");
        }
        //isn't fixed for hsqldb, and not tested for sqlserver
    }

    void down() {
    }
}