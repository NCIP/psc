class AddProviderMetadataToSites extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("studies", "provider", "string", limit: 255);
        addColumn("studies", "last_refresh", "timestamp");
    }

    void down() {
        removeColumn("studies", "provider");
        removeColumn("studies", "last_refresh"); 
    }
}