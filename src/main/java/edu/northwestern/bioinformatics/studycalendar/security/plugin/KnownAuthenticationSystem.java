package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration of the implementations of {@link AuthenticationSystem}
 * that are included with PSC.
 *
 * @author Rhett Sutphin
 */
public enum KnownAuthenticationSystem {
    LOCAL("local", LocalAuthenticationSystem.class, "uses passwords stored in PSC's own database"),
    CAS("CAS", CasAuthenticationSystem.class, "delegates authentication decisions to an enterprise-wide CAS server")
    ;

    private static final Logger log = LoggerFactory.getLogger(KnownAuthenticationSystem.class);

    private final String displayName;
    private final String description;
    private final Class<? extends AuthenticationSystem> systemClass;

    KnownAuthenticationSystem(
        String name, Class<? extends AuthenticationSystem> systemClass, String description
    ) {
        this.displayName = name;
        this.systemClass = systemClass;
        this.description = description;
    }

    public Class<? extends AuthenticationSystem> getAuthenticationSystemClass() {
        return systemClass;
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
