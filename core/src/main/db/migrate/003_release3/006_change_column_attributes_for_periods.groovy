class ChangeColumnAttributesForPeriods extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        setDefaultValue("periods", "duration_unit", "day");
        setDefaultValue("periods", "start_day", "1");
        setNullable("periods", "name", true);
    }

    void down() {
        setDefaultValue("periods", "duration_unit", null);
        setDefaultValue("periods", "start_day", null);
        // setNullable("periods", "name", false);
    }
}