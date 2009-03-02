class ModifyDeltasTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        dropColumn("deltas", "change_id")
    }

    void down() {
        addColumn("deltas", "change_id", "integer", nullable:false)
    }
}
