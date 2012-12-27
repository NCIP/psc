/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

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
import java.util.Comparator;
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
    // the order here is intended to roughly reflect workflow and is
    // the order the roles will appear in the user provisioning page
    STUDY_CREATOR,
    STUDY_CALENDAR_TEMPLATE_BUILDER,
    STUDY_QA_MANAGER,
    STUDY_SITE_PARTICIPATION_ADMINISTRATOR,

    STUDY_TEAM_ADMINISTRATOR,
    SUBJECT_MANAGER,
    STUDY_SUBJECT_CALENDAR_MANAGER,

    DATA_READER,

    SYSTEM_ADMINISTRATOR,
    USER_ADMINISTRATOR,
    DATA_IMPORTER,
    BUSINESS_ADMINISTRATOR,
    PERSON_AND_ORGANIZATION_INFORMATION_MANAGER,

    AE_REPORTER,
    LAB_DATA_USER,
    REGISTRAR
    ;

    public static Comparator<SuiteRole> ORDER = new Comparator<SuiteRole>() {
        public int compare(SuiteRole o1, SuiteRole o2) {
            PscRole pr1 = PscRole.valueOf(o1);
            PscRole pr2 = PscRole.valueOf(o2);
            if (pr1 != null && pr2 == null) {
                return -1;
            } else if (pr1 == null && pr2 != null) {
                return 1;
            } else if (pr1 != null) { // both PSC roles
                return pr1.ordinal() - pr2.ordinal();
            } else {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        }
    };

    private SuiteRole corresponding;
    private Collection<PscRoleUse> uses;
    private Collection<PscRoleGroup> groups;

    private static Properties roleProperties;
    private static PscRole[] withStudyAccess, provisionableByStudyTeamAdministrator, withSiteScoped;

    private PscRole() {
        this.corresponding = SuiteRole.valueOf(name());
        this.uses = createUses();
        this.groups = createGroups();
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

    public String getSuiteDescription() {
        return getSuiteRole().getDescription();
    }

    public String getDescription() {
        return getRoleProperties().getProperty(getCsmName() + ".description");
    }

    public String getScopeDescription() {
        String custom = getRoleProperties().getProperty(getCsmName() + ".scopeDescription");
        if (custom != null) {
            return custom;
        } else if (isStudyScoped()) {
            return getRoleProperties().getProperty("defaultScopeDescription.site+study");
        } else if (isSiteScoped()) {
            return getRoleProperties().getProperty("defaultScopeDescription.site");
        } else {
            return getRoleProperties().getProperty("defaultScopeDescription.global");
        }
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

    public Collection<PscRoleGroup> getGroups() {
        return groups;
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

    private Set<PscRoleGroup> createGroups() {
        String prop = getRoleProperties().getProperty(getCsmName() + ".groups");
        if (prop == null) {
            return Collections.emptySet();
        } else {
            Set<PscRoleGroup> creating = new LinkedHashSet<PscRoleGroup>();
            for (String scope : prop.split("\\s+")) {
                creating.add(PscRoleGroup.valueOf(scope.toUpperCase()));
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
            Set<PscRole> union = new LinkedHashSet<PscRole>();
            union.addAll(Arrays.asList(PscRoleUse.SITE_PARTICIPATION.roles()));
            union.addAll(Arrays.asList(PscRoleUse.TEMPLATE_MANAGEMENT.roles()));
            withStudyAccess = union.toArray(new PscRole[union.size()]);
        }
        return withStudyAccess;
    }

    /**
     * Those roles which can have their study scope provisioned by a Study Team Administrator.
     * This means the study-scoped roles which are used for site participation.
     */
    public static synchronized PscRole[] valuesProvisionableByStudyTeamAdministrator() {
        if (provisionableByStudyTeamAdministrator == null) {
            List<PscRole> provisionable =
                new ArrayList<PscRole>(PscRoleUse.SITE_PARTICIPATION.roles().length);
            for (PscRole role : PscRoleUse.SITE_PARTICIPATION.roles()) {
                if (role.isStudyScoped()) provisionable.add(role);
            }
            provisionableByStudyTeamAdministrator =
                provisionable.toArray(new PscRole[provisionable.size()]);
        }
        return provisionableByStudyTeamAdministrator;
    }

    /**
     * Those roles which have site scoped.
     */
    public static synchronized PscRole[] valuesWithSiteScoped() {
        if (withSiteScoped == null) {
            List<PscRole> siteScopedRoles =  new ArrayList<PscRole>(PscRole.values().length);;
            for (PscRole role :PscRole.values()) {
                if (role.isSiteScoped()) {
                    siteScopedRoles.add(role);
                }
            }
            withSiteScoped = siteScopedRoles.toArray(new PscRole[siteScopedRoles.size()]);
        }
        return withSiteScoped;
    }
}
