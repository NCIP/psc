class ModifyStudy extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('oracle')) {
            execute('ALTER TABLE studies ADD completed integer NOT NULL')
        } else {
            execute('ALTER TABLE studies ADD completed BOOLEAN NOT NULL')
        }
    }

    void down() {
        execute('ALTER TABLE studies DROP COLUMN completed')
    }
}
