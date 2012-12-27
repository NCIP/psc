/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import junit.framework.TestCase;

import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertNegative;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertPositive;

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

    public void testGroupsLoaded() throws Exception {
        assertTrue(PscRole.STUDY_QA_MANAGER.getGroups().contains(PscRoleGroup.TEMPLATE_MANAGEMENT));
        assertTrue(PscRole.STUDY_QA_MANAGER.getGroups().contains(PscRoleGroup.SITE_MANAGEMENT));
    }

    public void testAllRolesHaveAtLeastOneUse() throws Exception {
        for (PscRole role : PscRole.values()) {
            assertFalse("No uses for " + role, role.getUses().isEmpty());
        }
    }

    public void testNaturalOrderAlphabetizesSuiteOnlyRoles() throws Exception {
        assertOrder(
            SuiteRole.REGISTRATION_QA_MANAGER, SuiteRole.SUPPLEMENTAL_STUDY_INFORMATION_MANAGER);
        assertOrder(
            SuiteRole.AE_EXPEDITED_REPORT_REVIEWER, SuiteRole.AE_RULE_AND_REPORT_MANAGER);
    }

    public void testNaturalOrderPutsPscRolesInPscOrdinalOrder() throws Exception {
        assertOrder(SuiteRole.STUDY_CREATOR, SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
        assertOrder(SuiteRole.STUDY_QA_MANAGER, SuiteRole.STUDY_TEAM_ADMINISTRATOR);
        assertOrder(SuiteRole.SYSTEM_ADMINISTRATOR, SuiteRole.BUSINESS_ADMINISTRATOR);
    }

    public void testNaturalOrderPutsPscRolesBeforeSuiteOnlyRoles() throws Exception {
        assertOrder(SuiteRole.STUDY_QA_MANAGER, SuiteRole.AE_REPORTER);
    }

    private void assertOrder(SuiteRole first, SuiteRole second) {
        assertNegative(PscRole.ORDER.compare(first, second));
        assertPositive(PscRole.ORDER.compare(second, first));
    }
}
