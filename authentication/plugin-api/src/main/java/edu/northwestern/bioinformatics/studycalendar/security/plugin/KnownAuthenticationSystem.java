package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration of the implementations of {@link AuthenticationSystem}
 * that are included with PSC.
 *
 * @author Rhett Sutphin
 */
@Deprecated // use bundle metadata instead
public enum KnownAuthenticationSystem {
    LOCAL("local",
        "edu.northwestern.bioinformatics.studycalendar.security.plugin.local.LocalAuthenticationSystem",
        "uses passwords stored in PSC's own database"),
    CAS("CAS",
        "edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasAuthenticationSystem",
        "delegates authentication decisions to an enterprise-wide CAS server"),
    WEBSSO("caGrid WebSSO",
        "edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasAuthenticationSystem",
        "delegates authentication to a caGrid WebSSO server (use this option for CCTS)")
    ;

    private static final Logger log = LoggerFactory.getLogger(KnownAuthenticationSystem.class);

    private final String displayName;
    private final String description;
    private final String systemClassName;

    KnownAuthenticationSystem(
        String name, String systemClassName, String description
    ) {
        this.displayName = name;
        this.systemClassName = systemClassName;
        this.description = description;
    }

    @SuppressWarnings({"unchecked"})
    public Class<? extends AuthenticationSystem> getAuthenticationSystemClass() {
        // this is temporary
        try {
            return (Class<? extends AuthenticationSystem>) Class.forName(systemClassName);
        } catch (ClassNotFoundException e) {
            throw new edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException(e);
        }
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Like {@link #valueOf}, except that it returns null if the name doesn't
     * map to a value in this enum.
     */
    public static KnownAuthenticationSystem safeValueOf(String name) {
        try {
            return name == null ? null : valueOf(name);
        } catch (IllegalArgumentException iae) {
            log.debug("No {} named {}", KnownAuthenticationSystem.class.getSimpleName(), name);
            return null;
        }
    }
}
