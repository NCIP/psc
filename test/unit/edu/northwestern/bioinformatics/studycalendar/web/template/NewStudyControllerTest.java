package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class NewStudyControllerTest extends ControllerTestCase {
    private static final int ID = 81;

    private NewStudyController controller;
    private StudyDao studyDao;
    private StudySiteDao studySiteDao;
    private SiteDao siteDao;

    protected void setUp() throws Exception {
        super.setUp();
        request.setMethod("GET");
        studyDao = registerMockFor(StudyDao.class);
        studySiteDao = registerMockFor(StudySiteDao.class);
        siteDao = registerMockFor(SiteDao.class);

        controller = new NewStudyController();
        controller.setStudyDao(studyDao);
        controller.setSiteDao(siteDao);
        controller.setStudySiteDao(studySiteDao);
    }

    public void testHandle() throws Exception {
        studyDao.save(newStudy());

        // these are temporary
        expect(siteDao.getDefaultSite()).andReturn(new Site());
        studySiteDao.save((StudySite) notNull());

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("redirectToCalendarTemplate", mv.getViewName());
        assertEquals(1, mv.getModel().size());
        assertContainsPair(mv.getModel(), "study", ID);
    }

    private static Study newStudy() {
        EasyMock.reportMatcher(new NewStudyMatcher());
        return setId(ID, new Study());
    }
    
    private static class NewStudyMatcher implements IArgumentMatcher {

        public boolean matches(Object argument) {
            Study actual = (Study) argument;
            if (!"New study".equals(actual.getName())) return false;

            List<Epoch> epochs = actual.getPlannedCalendar().getEpochs();
            if (epochs.size() != 1) return false;
            if (!"New epoch".equals(epochs.get(0).getName())) return false;

            return true;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("new, blank study");
        }
    }
}
