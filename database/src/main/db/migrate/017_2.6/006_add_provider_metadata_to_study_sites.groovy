class AddProviderMetadataToStudySites extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("study_sites", "provider", "string", limit: 255);
        addColumn("study_sites", "last_refresh", "timestamp");
    }

    void down() {
        removeColumn("study_sites", "provider");
        removeColumn("study_sites", "last_refresh");
    }
}