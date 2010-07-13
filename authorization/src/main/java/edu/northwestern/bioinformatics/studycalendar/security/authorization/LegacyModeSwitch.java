package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Spring-configured singleton which indicates whether to use the legacy
 * (local domain) or new (CSM-based unified auth) authorization system.
 *
 * @author Rhett Sutphin
 */
@Deprecated
public class LegacyModeSwitch implements InitializingBean {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private boolean on;

    public LegacyModeSwitch() {
        this(true);
    }

    public LegacyModeSwitch(boolean on) {
        this.on = on;
    }

    public void afterPropertiesSet() throws Exception {
        log.info("Authorization is in {} mode", isOn() ? "legacy" : "unified");
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }
}
