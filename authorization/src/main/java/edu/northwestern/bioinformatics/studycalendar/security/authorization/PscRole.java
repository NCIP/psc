package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import org.acegisecurity.GrantedAuthority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * An enumeration of the suite roles which PSC cares about.
 *
 * @author Rhett Sutphin
 */
public enum PscRole implements GrantedAuthority {
    SYSTEM_ADMINISTRATOR,
    BUSINESS_ADMINISTRATOR,
    PERSON_AND_ORGANIZATION_INFORMATION_MANAGER,
    DATA_IMPORTER,
    USER_ADMINISTRATOR,
    STUDY_QA_MANAGER,
    STUDY_CREATOR,
    STUDY_TEAM_ADMINISTRATOR,
    STUDY_SITE_PARTICIPATION_ADMINISTRATOR,
    STUDY_CALENDAR_TEMPLATE_BUILDER,
    SUBJECT_MANAGER,
    STUDY_SUBJECT_CALENDAR_MANAGER,
    REGISTRAR,
    AE_REPORTER,
    LAB_DATA_USER,
    DATA_READER,
    ;

    private SuiteRole corresponding;
    private Collection<PscRoleUse> uses;

    private static Properties roleProperties;
    private static PscRole[] withStudyAccess;

    private PscRole() {
        this.corresponding = SuiteRole.valueOf(name());
        this.uses = createUses();
    }

    public static PscRole valueOf(SuiteRole suiteRole) {
        for (PscRole pscRole : values()) {
            if (suiteRole == pscRole.getSuiteRole()) return pscRole;
        }
        return null;
    }

    public SuiteRole getSuiteRole() {
        return corresponding;
    }

    public String getDescription() {
        return getSuiteRole().getDescription();
    }

    public String getDisplayName() {
        return getSuiteRole().getDisplayName();
    }

    public String getCsmName() {
        return getSuiteRole().getCsmName();
    }

    public Set<ScopeType> getScopes() {
        return getSuiteRole().getScopes();
    }

    public boolean isStudyScoped() {
        return getSuiteRole().isStudyScoped();
    }

    public boolean isSiteScoped() {
        return getSuiteRole().isSiteScoped();
    }

    public boolean isScoped() {
        return getSuiteRole().isScoped();
    }

    public String getAuthority() {
        return getCsmName();
    }

    public Collection<PscRoleUse> getUses() {
        return uses;
    }

    private Set<PscRoleUse> createUses() {
        String prop = getRoleProperties().getProperty(getCsmName() + ".uses");
        if (prop == null) {
            return Collections.emptySet();
        } else {
            Set<PscRoleUse> creating = new LinkedHashSet<PscRoleUse>();
            for (String scope : prop.split("\\s+")) {
                creating.add(PscRoleUse.valueOf(scope.toUpperCase()));
            }
            return Collections.unmodifiableSet(creating);
        }
    }

    /**
     * Loads and returns the role.properties resource.  This resource contains all non-default
     * information about each role.
     */
    private synchronized static Properties getRoleProperties() {
        if (roleProperties == null) {
            roleProperties = new Properties();
            try {
                roleProperties.load(PscRole.class.getResourceAsStream("psc-role.properties"));
            } catch (IOException e) {
                throw new StudyCalendarError("Cannot load role info from properties", e);
            }
        }
        return roleProperties;
    }

    /**
     * Those roles which have any sort of access to a study.
     */
    public static synchronized PscRole[] valuesWithStudyAccess() {
        if (withStudyAccess == null) {
            List<PscRole> filtered = new ArrayList<PscRole>(Arrays.asList(values()));
            for (Iterator<PscRole> it = filtered.iterator(); it.hasNext();) {
                PscRole pscRole = it.next();
                if (!pscRole.getUses().contains(PscRoleUse.SITE_PARTICIPATION) && !pscRole.getUses().contains(PscRoleUse.TEMPLATE_MANAGEMENT)) {
                    it.remove();
                }
            }
            withStudyAccess = filtered.toArray(new PscRole[filtered.size()]);
        }
        return withStudyAccess;
    }
}
