class ManagingSites extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
      createTable("managing_sites")  { t ->
          t.addColumn("study_id", "integer", nullable: false);
          t.addColumn("site_id", "integer", nullable: false);
      }
      execute("ALTER TABLE managing_sites ADD CONSTRAINT fk_study_mgsite_study FOREIGN KEY (study_id) REFERENCES studies");
      execute("ALTER TABLE managing_sites ADD CONSTRAINT fk_study_mgsite_site FOREIGN KEY (site_id) REFERENCES sites");
    }

    void down() {
        dropTable('managing_sites');
    }
}
