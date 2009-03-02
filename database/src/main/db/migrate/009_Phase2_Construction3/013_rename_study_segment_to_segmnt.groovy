class RenameStudySegmentToSegment extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches('postgres')) {
            execute("UPDATE delta_node_types SET node_type='segmnt' WHERE node_type='studysegment'");
            execute("UPDATE deltas SET node_type='segmnt' WHERE node_type='studysegment'");
        }
    }

    void down() {
        if (databaseMatches('postgres')) {
            execute("UPDATE delta_node_types SET node_type='studysegment' WHERE node_type='segmnt'");
            execute("UPDATE deltas SET node_type='studysegment' WHERE node_type='segmnt'");
        }
    }

}





