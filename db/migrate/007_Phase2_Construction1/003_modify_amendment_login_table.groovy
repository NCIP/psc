class ModifyAmendmentLoginTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameTable("amendment_logins", "amendments", primaryKey: false)
        //renameTable("amendment_logins_id_seq", "amendments_id_seq", primaryKey: false)
        //renameTable("amendment_logins_pkey", "amendment_pkey", primaryKey: false)

        addColumn("amendments", "previous_amendment", "integer", nullable:true)
        addColumn("amendments", "name", "string")

        dropColumn("amendments", "amendment_number")

    }

    void down() {
        dropColumn("amendments", "previous_amendment")
        dropColumn("amendments", "name")

        //renameTable("amendments_id_seq", "amendment_logins_id_seq", primaryKey: false)
        //renameTable("amendment_pkey", "amendment_logins_pkey", primaryKey: false)
        renameTable("amendments", "amendment_logins", primaryKey: false)

        addColumn("amendment_logins", "amendment_number", "integer", nullable: false)
    }
}
