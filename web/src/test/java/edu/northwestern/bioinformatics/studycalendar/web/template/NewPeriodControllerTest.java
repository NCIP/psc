package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;

import java.util.Collection;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;

/**
 * @author Rhett Sutphin
 */
public class NewPeriodControllerTest extends ControllerTestCase {
    private NewPeriodController controller;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller = new NewPeriodController();
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, STUDY_CALENDAR_TEMPLATE_BUILDER);
    }    

    public void testCommandForRegularNewPeriod() throws Exception {
        Object actual = controller.formBackingObject(request);
        assertTrue(actual instanceof NewPeriodCommand);
    }

    public void testCommandForCopyNewPeriod() throws Exception {
        request.setParameter("selectedPeriod", "7");
        Object actual = controller.formBackingObject(request);
        assertTrue(actual instanceof CopyPeriodCommand);
    }
}
