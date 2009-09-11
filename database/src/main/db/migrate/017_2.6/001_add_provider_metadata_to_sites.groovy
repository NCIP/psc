class AddProviderMetadataToSites extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("sites", "provider", "string", limit: 255);
        addColumn("sites", "last_refresh", "timestamp");
    }

    void down() {
        removeColumn("sites", "provider");
        removeColumn("sites", "last_refresh"); 
    }
}