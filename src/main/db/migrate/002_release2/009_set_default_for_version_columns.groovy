class SetDefaultForVersionColumns extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        setDefaultValue("activity_types",   "version", "0");
        setDefaultValue("arms",             "version", "0");
        setDefaultValue("activities",       "version", "0");
        setDefaultValue("participant_identifiers", "version", "0");
        setDefaultValue("participants",     "version", "0");
        setDefaultValue("participant_assignments", "version", "0");
        setDefaultValue("planned_events",   "version", "0");
        setDefaultValue("planned_schedules", "version", "0");
        setDefaultValue("sites",            "version", "0");
        setDefaultValue("studies",          "version", "0");
        setDefaultValue("study_sites",      "version", "0");
    }

    void down() {
        setDefaultValue("activity_types",   "version", null);
        setDefaultValue("arms",             "version", null);
        setDefaultValue("activities",       "version", null);
        setDefaultValue("participant_identifiers", "version", null);
        setDefaultValue("participants",     "version", null);
        setDefaultValue("participant_assignments", "version", null);
        setDefaultValue("planned_events",   "version", null);
        setDefaultValue("planned_schedules", "version", null);
        setDefaultValue("sites",            "version", null);
        setDefaultValue("studies",          "version", null);
        setDefaultValue("study_sites",      "version", null);
    }
}
