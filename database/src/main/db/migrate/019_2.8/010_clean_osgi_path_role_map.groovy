/*
   The old authorization system injected a list of all the controller URLs in the system
   into the OSGi layer at startup.  This migration purges those items from the persistent
   OSGi configuration in the database.
 */
class CleanOsgiPathRoleMap extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("DELETE FROM osgi_cm_property_values WHERE property_id IN (SELECT id FROM osgi_cm_properties WHERE service_pid='edu.northwestern.bioinformatics.studycalendar.security.filter-security-configurer')");
        execute("DELETE FROM osgi_cm_properties WHERE service_pid='edu.northwestern.bioinformatics.studycalendar.security.filter-security-configurer'");
    }

    void down() {
    }
}
