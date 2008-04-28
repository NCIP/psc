class CreateUsers extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable('users') { t ->
            t.addVersionColumn()
            t.addColumn('name', 'string', nullable: false, limit: 255)
            t.addColumn('csm_user_id', 'integer', nullable:false)
        }
    }

    void down() {
        dropTable('users')
    }
}
