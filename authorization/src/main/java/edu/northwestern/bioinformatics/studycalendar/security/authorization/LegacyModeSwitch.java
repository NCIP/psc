package edu.northwestern.bioinformatics.studycalendar.security.authorization;

/**
 * Spring-configured singleton which indicates whether to use the legacy
 * (local domain) or new (CSM-based unified auth) authorization system.
 *
 * @author Rhett Sutphin
 */
@Deprecated
public class LegacyModeSwitch {
    private boolean on;

    public LegacyModeSwitch() {
        this(true);
    }

    public LegacyModeSwitch(boolean on) {
        this.on = on;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }
}
