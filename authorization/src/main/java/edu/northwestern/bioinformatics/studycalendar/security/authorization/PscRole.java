package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import org.acegisecurity.GrantedAuthority;

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

    private PscRole() {
        this.corresponding = SuiteRole.valueOf(name());
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
}
