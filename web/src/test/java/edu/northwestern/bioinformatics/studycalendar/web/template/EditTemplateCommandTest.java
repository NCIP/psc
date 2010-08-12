package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;

import java.util.Collection;
import java.util.Iterator;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class EditTemplateCommandTest extends StudyCalendarTestCase {
    private EditTemplateCommand command;
    private StudyService studyService;

    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyService = registerMockFor(StudyService.class);

        command = registerMockFor(EditTemplateCommand.class,
            EditTemplateCommand.class.getMethod("validAction"),
            EditTemplateCommand.class.getMethod("performEdit"),
            EditTemplateCommand.class.getMethod("getRelativeViewName")
        );
        command.setStudyService(studyService);
        command.setDeltaService(Fixtures.getTestingDeltaService());

        study = Fixtures.createSingleEpochStudy("Study 1234", "E1", "A", "B");
        study.getPlannedCalendar().addEpoch(Epoch.create("E2"));
        study.setDevelopmentAmendment(new Amendment());
    }

    public void testApply() throws Exception {
        command.setStudy(study);
        expect(command.validAction()).andReturn(true);
        command.performEdit();
        studyService.save(study);

        replayMocks();
        command.apply();
        verifyMocks();
    }
    
    public void testApplyToCompleteCalendar() throws Exception {
        study.setDevelopmentAmendment(null);

        try {
            command.setStudy(study);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException e) {
            assertContains(e.getMessage(), study.getName());
            assertContains(e.getMessage(), "not in development");
        }
    }

    public void testAuthorizedForBuilderFromManagingSites() throws Exception {
        command.setStudy(study);
        study.addManagingSite(Fixtures.createSite("B", "B'"));

        Collection<ResourceAuthorization> actual = command.authorizations(null);
        assertEquals("Wrong number of authorizations", 2, actual.size());
        ResourceAuthorization actualAuth = actual.iterator().next();
        assertEquals("Wrong role", PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER, actualAuth.getRole());
        assertEquals("Wrong study", "Study 1234", actualAuth.getScope(ScopeType.STUDY));
        assertEquals("Wrong site", "B'", actualAuth.getScope(ScopeType.SITE));
    }

    public void testAuthorizesForBuilderAndCreator() throws Exception {
        command.setStudy(study);
        study.addManagingSite(Fixtures.createSite("B", "B'"));
        Collection<ResourceAuthorization> actual = command.authorizations(null);
        assertEquals("Wrong number of authorizations", 2, actual.size());
        Iterator itr = actual.iterator();
        ResourceAuthorization actualAuth1 = (ResourceAuthorization)itr.next();
        ResourceAuthorization actualAuth2 = (ResourceAuthorization)itr.next();
        assertEquals("Wrong role", PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER, actualAuth1.getRole());
        assertEquals("Wrong role", PscRole.STUDY_CREATOR, actualAuth2.getRole());
    }
}
