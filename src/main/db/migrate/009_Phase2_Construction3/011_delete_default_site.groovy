class DeleteDefaultSite extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        // Instead of deleting, just cancel the original migration (2|14)
        // execute("DELETE FROM sites WHERE name='default'");
    }

    void down() {
        // Instead of deleting, just cancel the original migration (2|14)
        // insert("sites", [ name: 'default' ])
    }
}