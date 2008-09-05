class MoveAmendmentUpdatedDateToChange extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("changes", "updated_date", "timestamp")
        execute(
          "UPDATE changes SET updated_date=" +
             "(SELECT updated_date FROM amendments a INNER JOIN deltas d on d.amendment_id=a.id WHERE changes.delta_id=d.id)")
        removeColumn("amendments", "updated_date")
    }

    void down() {
        addColumn("amendments", "updated_date", "timestamp")
        execute(
          "UPDATE amendments SET updated_date=" +
            "(SELECT MAX(c.updated_date) FROM changes c INNER JOIN deltas d on c.delta_id=d.id WHERE d.amendment_id=amendments.id)")
        removeColumn("changes", "updated_date")
    }
}