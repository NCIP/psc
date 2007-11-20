// Reverses 2|14
class DeleteDefaultSite extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("DELETE FROM sites WHERE name='default'");
    }

    void down() {
        insert("sites", [ name: 'default' ])
    }
}