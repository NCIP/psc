// In a real belt-and-suspenders move, the arm-epoch relationship had both
// a normal not-null setting (dropped in 7-15) and a check constraint
class DetachPlanTreeNodesMore extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("ALTER TABLE arms DROP CONSTRAINT nn_arm_epoch")
    }

    void down() {
        // from 2-8
        execute('ALTER TABLE arms ADD CONSTRAINT nn_arm_epoch CHECK (epoch_id IS NOT NULL)')
    }
}
