// XXX: As written, this migration cannot be taken down and then back up
class CreateBaseSitePg extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        String fileName = "db/migrate/003_release3/BaseSitePG_create.sql"
        File input = new File(fileName)
        String content = input.text
        execute(content)
    }
    void down() {
         String fileName = "db/migrate/003_release3/BaseSitePG_drop.sql"
         File input = new File(fileName)
         String content = input.text
         execute(content)
    }
}

