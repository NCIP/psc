class MakeAmendmentDateARealDate extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        dropColumn("amendments", "amendment_date")
        addColumn("amendments", "amendment_date", "date")
        execute("UPDATE amendments SET amendment_date='2001-01-01'")
        setNullable("amendments", "amendment_date", false)
    }

    void down() {
        dropColumn("amendments", "amendment_date")
        addColumn("amendments", "amendment_date", "string")
        execute("UPDATE amendments SET amendment_date='lost in migration'")
        setNullable("amendments", "amendment_date", false)
    }
}
