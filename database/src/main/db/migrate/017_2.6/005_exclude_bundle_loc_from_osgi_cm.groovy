class ExcludeBundleLocFromOsgiCm extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("DELETE FROM osgi_cm_property_values WHERE property_id IN (SELECT id FROM osgi_cm_properties WHERE name='service.bundleLocation')");
        execute("DELETE FROM osgi_cm_properties WHERE name='service.bundleLocation'"); 
    }

    void down() {
    }
}