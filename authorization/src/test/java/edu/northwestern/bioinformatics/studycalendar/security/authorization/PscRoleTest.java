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

    public void testDescriptionDelegatesToSuiteRole() throws Exception {
        assertEquals(
            SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getDescription(),
            PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getDescription()
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
}
