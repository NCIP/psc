class ModifyStudy extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('oracle')) {
            execute('ALTER TABLE studies ADD completed integer DEFAULT \'0\' NOT NULL')
        } else {
            execute('ALTER TABLE studies ADD completed BOOLEAN DEFAULT false NOT NULL')
        }
    }

    void down() {
        execute('ALTER TABLE studies DROP COLUMN completed')
    }
}
