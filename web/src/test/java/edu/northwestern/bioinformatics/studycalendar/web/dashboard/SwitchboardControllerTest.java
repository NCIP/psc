/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.dashboard;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class SwitchboardControllerTest extends ControllerTestCase {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private SwitchboardController controller;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        controller = new SwitchboardController();
        controller.setApplicationSecurityManager(applicationSecurityManager);
    }

    ////// TARGETING

    public void testRendersDashboardForStudySubjectCalendarManager() throws Exception {
        SecurityContextHolderTestHelper.
            setUserAndReturnMembership("ice", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);
        ModelAndView mv = doHandle();
        assertEquals("redirectToDashboard", mv.getViewName());
    }

    public void testRendersStudyListForStudyCalendarTemplateBuilder() throws Exception {
        SecurityContextHolderTestHelper.
            setUserAndReturnMembership("ice", PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
        ModelAndView mv = doHandle();
        assertEquals("redirectToStudyList", mv.getViewName());
    }

    public void testRendersStudyListForStudyQaManager() throws Exception {
        SecurityContextHolderTestHelper.
            setUserAndReturnMembership("ice", PscRole.STUDY_QA_MANAGER);
        ModelAndView mv = doHandle();
        assertEquals("redirectToStudyList", mv.getViewName());
    }

    public void testRendersStudyListForParticipationAdmin() throws Exception {
        SecurityContextHolderTestHelper.
            setUserAndReturnMembership("ice", PscRole.STUDY_SITE_PARTICIPATION_ADMINISTRATOR);
        ModelAndView mv = doHandle();
        assertEquals("redirectToStudyList", mv.getViewName());
    }

    public void testRendersStudyListForDataReader() throws Exception {
        SecurityContextHolderTestHelper.
            setUserAndReturnMembership("ice", PscRole.DATA_READER);
        ModelAndView mv = doHandle();
        assertEquals("redirectToStudyList", mv.getViewName());
    }

    public void testRendersActivityPageForBusinessAdmin() throws Exception {
        SecurityContextHolderTestHelper.
            setUserAndReturnMembership("ice", PscRole.BUSINESS_ADMINISTRATOR);
        ModelAndView mv = doHandle();
        assertEquals("redirectToActivities", mv.getViewName());
    }

    public void testRendersAdminPageForSysAdmin() throws Exception {
        SecurityContextHolderTestHelper.
            setUserAndReturnMembership("ice", PscRole.SYSTEM_ADMINISTRATOR);
        ModelAndView mv = doHandle();
        assertEquals("redirectToAdministration", mv.getViewName());
    }

    public void testRendersAdminPageForUserAdmin() throws Exception {
        SecurityContextHolderTestHelper.
            setUserAndReturnMembership("ice", PscRole.USER_ADMINISTRATOR);
        ModelAndView mv = doHandle();
        assertEquals("redirectToAdministration", mv.getViewName());
    }

    public void testRendersAdminPageForPOIM() throws Exception {
        SecurityContextHolderTestHelper.
            setUserAndReturnMembership("ice", PscRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
        ModelAndView mv = doHandle();
        assertEquals("redirectToAdministration", mv.getViewName());
    }

    public void testPrefersDashboardToStudyList() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(new PscUserBuilder().
            add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies().
            add(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites().forAllStudies().
            toUser());
        ModelAndView mv = doHandle();
        assertEquals("redirectToDashboard", mv.getViewName());
    }

    public void testPrefersStudyListToAdmin() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(new PscUserBuilder().
            add(PscRole.SYSTEM_ADMINISTRATOR).
            add(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites().forAllStudies().
            toUser());
        ModelAndView mv = doHandle();
        assertEquals("redirectToStudyList", mv.getViewName());
    }

    public void testAccessoryRolesForbidden() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(new PscUserBuilder().
            add(PscRole.AE_REPORTER).forAllSites().forAllStudies().
            toUser());
        ModelAndView mv = doHandle();
        assertNull(mv);
        assertEquals(403, response.getStatus());
    }

    public void testGridServiceRolesForbidden() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(new PscUserBuilder().
            add(PscRole.REGISTRAR).forAllSites().forAllStudies().
            toUser());
        ModelAndView mv = doHandle();
        assertNull(mv);
        assertEquals(403, response.getStatus());
    }

    public void testNoRolesForbidden() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(new PscUserBuilder().toUser());
        ModelAndView mv = doHandle();
        assertNull(mv);
        assertEquals(403, response.getStatus());
    }

    public void testAllUsableRolesHaveResponses() throws Exception {
        List<PscRole> badRoles = new LinkedList<PscRole>();
        for (PscRole pscRole : PscRole.values()) {
            SecurityContextHolderTestHelper.setUserAndReturnMembership("jo", pscRole);
            if (pscRole.getUses().contains(PscRoleUse.ACCESSORY)) {
                continue;
            } else if (pscRole.getUses().equals(Collections.singleton(PscRoleUse.GRID_SERVICES))) {
                continue;
            }
            try {
                doHandle();
            } catch (StudyCalendarSystemException failure) {
                log.info("{} failed with {}", pscRole, failure.getMessage());
                badRoles.add(pscRole);
            }
        }
        if (!badRoles.isEmpty()) {
            fail("These roles had no response: " + badRoles);
        }
    }

    private ModelAndView doHandle() throws Exception {
        return controller.handleRequest(request, response);
    }
}
