class ModifyParticipants extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('hsqldb')) {
            execute('ALTER TABLE participants ALTER COLUMN social_security_number RENAME TO person_id')
        } else {
            execute('ALTER TABLE participants RENAME COLUMN social_security_number TO person_id')
        }
    }

    void down() {
        if (databaseMatches('hsqldb')) {
            execute('ALTER TABLE participants ALTER COLUMN person_id RENAME TO social_security_number')
        } else {
            execute('ALTER TABLE participants RENAME COLUMN person_id TO social_security_number')
        }
    }
}