class AddOsgiCmPersistence extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("osgi_cm_properties") { t ->
            t.addVersionColumn();
            t.addColumn("service_pid", "string", limit: 1024)
            t.addColumn("name", "string", limit: 1024)
            t.addColumn("collection_kind", "string", limit: 10)
            t.addColumn("kind", "string", limit: 8) // since "type" is reserved
        }

        createTable("osgi_cm_property_values") { t ->
            t.includePrimaryKey = false
            t.addColumn("list_index", "integer", nullable: false, defaultValue: 0)
            t.addColumn("property_id", "integer", nullable: false, 
                references: "osgi_cm_properties", referenceName: "fk_osgi_cm_prop_value")
            t.addColumn("value", "string", limit: 1024)
        }
    }

    void down() {
        dropTable("osgi_cm_property_values");
        dropTable("osgi_cm_properties");
    }
}