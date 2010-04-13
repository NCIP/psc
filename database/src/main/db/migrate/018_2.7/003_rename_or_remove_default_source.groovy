class RenameOrRemoveDefaultSource extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("DELETE from sources s WHERE s.name='PSC - Manual Activity Creation' AND not exists (SELECT source_id from activities a WHERE a.source_id=s.id)");
        execute("UPDATE sources s SET name=name||' - ${java.util.UUID.randomUUID().toString()}' WHERE s.name='PSC - Manual Activity Creation' AND exists (SELECT source_id from activities a WHERE a.source_id=s.id)");
    }

    void down() {
    }
}
