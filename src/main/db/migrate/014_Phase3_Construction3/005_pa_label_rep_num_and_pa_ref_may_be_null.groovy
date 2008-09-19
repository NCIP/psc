class PaLabelRepNumAndPaRefMayBeNull extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        setNullable("planned_activity_labels", "rep_num", true)
        setNullable("planned_activity_labels", "planned_activity_id", true)
    }

    void down() {
        setNullable("planned_activity_labels", "planned_activity_id", false)
        setNullable("planned_activity_labels", "rep_num", false)
    }
}