class InsertDefaultSite extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        // Disabled for new deployments as of release 009
        // insert("sites", [ name: 'default' ])
    }

    void down() {
        // Disabled for new deployments as of release 009
        // execute("DELETE FROM sites WHERE name='default'");
    }
}
