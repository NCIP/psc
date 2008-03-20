class ModifyScheduledEventModes extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("insert into scheduled_event_modes (id, name) values ('4', 'conditional')");
        execute("insert into scheduled_event_modes (id, name) values ('5', 'NA')");
    }

    void down() {
        execute("delete from scheduled_event_modes where id=4");
        execute("delete from scheduled_event_modes where id=5");
    }
}
