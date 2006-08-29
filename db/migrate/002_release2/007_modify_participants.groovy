class ModifyStudy extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute('ALTER TABLE participants RENAME COLUMN social_security_number TO person_id')
    }

    void down() {
        execute('ALTER TABLE participants RENAME COLUMN person_id TO social_security_number')
    }
}
