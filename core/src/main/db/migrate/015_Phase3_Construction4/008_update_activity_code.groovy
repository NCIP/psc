class UpdateActivityCode extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("UPDATE activities SET code = name  WHERE code=''");
        execute("UPDATE activities SET code = name  WHERE code is null");
    }

    void down() {

    }
}