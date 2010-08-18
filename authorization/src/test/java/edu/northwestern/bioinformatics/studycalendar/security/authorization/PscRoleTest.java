package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import junit.framework.TestCase;

/**
 * @author Rhett Sutphin
 */
public class PscRoleTest extends TestCase {
    public void testGrantedAuthorityName() throws Exception {
        assertEquals("study_qa_manager", PscRole.STUDY_QA_MANAGER.getAuthority());
    }

    public void testSuiteRoleIsCorrect() throws Exception {
        assertSame(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER,
            PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getSuiteRole());
    }

    public void testAllSuiteRolesResolvable() throws Exception {
        for (PscRole r : PscRole.values()) {
            r.getSuiteRole();
        }
        // no exceptions
    }

    public void testAllRolesHavePscDescriptions() throws Exception {
        for (PscRole role : PscRole.values()) {
            assertNotNull("Role missing description: " + role, role.getDescription());
        }
    }

    public void testScopeDescriptionForGlobalRole() throws Exception {
        assertEquals("This user's actions are global, meaning that they affect the entire application and are not limited by site or study.",
            PscRole.DATA_IMPORTER.getScopeDescription());
    }

    public void testScopeDescriptionForSiteScopedRole() throws Exception {
        assertEquals("This user's actions can be limited to one, many, or opened to all sites.",
            PscRole.STUDY_TEAM_ADMINISTRATOR.getScopeDescription());
    }

    public void testScopeDescriptionForSiteAndStudyScopedRole() throws Exception {
        assertEquals("This user's actions can be limited to one, many, or opened to all sites and further limited to one, many, or all studies within those sites.",
            PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getScopeDescription());
    }

    public void testScopeDescriptionForRoleSpecificCustomDescription() throws Exception {
        assertEquals("This user's actions can be limited to one, many, or opened to all sites. This user's actions are further refined by the role the site is playing in the study: either participating or managing.",
            PscRole.STUDY_QA_MANAGER.getScopeDescription());
    }

    public void testSuiteDescriptionDelegatesToSuiteRole() throws Exception {
        assertEquals(
            SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getDescription(),
            PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getSuiteDescription()
        );
    }

    public void testDisplayNameDelegatesToSuiteRole() throws Exception {
        assertEquals(
            SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getDisplayName(),
            PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getDisplayName()
        );
    }

    public void testScopesDelegatesToSuiteRole() throws Exception {
        assertEquals(
            SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getScopes(),
            PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getScopes()
        );
    }

    public void testCsmNameDelegatesToSuiteRole() throws Exception {
        assertEquals(
            SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getCsmName(),
            PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getCsmName()
        );
    }

    public void testIsSiteScoped() throws Exception {
        assertTrue(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER.isSiteScoped());
        assertFalse(PscRole.BUSINESS_ADMINISTRATOR.isSiteScoped());
    }

    public void testIsStudyScoped() throws Exception {
        assertTrue(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER.isStudyScoped());
        assertFalse(PscRole.STUDY_QA_MANAGER.isStudyScoped());
    }

    public void testGetCorrespondingSuiteRole() throws Exception {
        assertSame(PscRole.STUDY_QA_MANAGER, PscRole.valueOf(SuiteRole.STUDY_QA_MANAGER));
    }

    public void testGetCorrespondingSuiteRoleWhenNone() throws Exception {
        assertNull(PscRole.valueOf(SuiteRole.DATA_ANALYST));
    }

    public void testUsesLoaded() throws Exception {
        assertTrue(PscRole.STUDY_QA_MANAGER.getUses().contains(PscRoleUse.TEMPLATE_MANAGEMENT));
        assertTrue(PscRole.STUDY_QA_MANAGER.getUses().contains(PscRoleUse.SITE_PARTICIPATION));
    }

    public void testAllRolesHaveAtLeastOneUse() throws Exception {
        for (PscRole role : PscRole.values()) {
            assertFalse("No uses for " + role, role.getUses().isEmpty());
        }
    }
}
