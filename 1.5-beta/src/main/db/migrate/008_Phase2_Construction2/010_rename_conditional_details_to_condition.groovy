class RenameConditionalDetailsToCondition extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameColumn("planned_events", "conditional_details", "condition");
        execute("UPDATE changes SET attribute='condition' WHERE attribute='conditionalDetails'");
    }

    void down() {
        execute("UPDATE changes SET attribute='conditionalDetails' WHERE attribute='condition'");
        renameColumn("planned_events", "condition", "conditional_details");
    }
}
