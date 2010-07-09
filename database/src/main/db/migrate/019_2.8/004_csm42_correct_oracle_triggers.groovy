class Csm42CorrectOracleTriggers extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('oracle')) {
            external('004_csm42_correct_oracle_triggers.sql');
        }
    }

    void down() {
        // Unnecessary
    }
}
