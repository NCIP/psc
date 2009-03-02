// Drop all the constraints that prevent plan tree nodes from existing without a parent
class DetachPlanTreeNodes extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        setNullable("periods", "arm_id", true)
        setNullable("planned_events", "period_id", true)
    }

    void down() {
        // Note that this will fail if there is data in the database
        setNullable("periods", "arm_id", false)
        setNullable("planned_events", "period_id", false)
    }
}
