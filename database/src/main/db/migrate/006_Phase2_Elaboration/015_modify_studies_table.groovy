class ModifyStudiesTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("studies", "amended", "boolean", nullable: false , defaultValue:"0");
    }

    void down() {
        dropColumn("studies", "amended");
    }
}