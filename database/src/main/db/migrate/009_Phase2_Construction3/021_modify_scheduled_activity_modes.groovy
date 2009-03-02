class ModifyScheduledActivityModes extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("insert into scheduled_activity_modes (id, name) values ('6', 'missed')");
    }

    void down() {
        execute("delete from scheduled_activity_modes where id=6");
    }
}