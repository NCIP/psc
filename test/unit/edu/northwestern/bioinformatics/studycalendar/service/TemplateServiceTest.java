package edu.northwestern.bioinformatics.studycalendar.service;

import static org.easymock.EasyMock.expectLastCall;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;

import java.util.Arrays;
import java.util.List;

import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class TemplateServiceTest extends StudyCalendarTestCase {
    private TemplateService service;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudySiteDao studySiteDao;
    private StudyCalendarAuthorizationManager authorizationManager;

    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        studySiteDao = registerDaoMockFor(StudySiteDao.class);
        authorizationManager = registerMockFor(StudyCalendarAuthorizationManager.class);

        service = new TemplateService();
        service.setStudyDao(studyDao);
        service.setSiteDao(siteDao);
        service.setStudyCalendarAuthorizationManager(authorizationManager);
        service.setStudySiteDao(studySiteDao);
    }

    public void testCannotRemoveStudySiteWithAssociatedAssignments() throws Exception {
        Study study = createNamedInstance("ECOG 1234", Study.class);
        Site site1 = setId(1, createNamedInstance("Mayo", Site.class));
        Site site2 = createNamedInstance("Dartmouth", Site.class);
        StudySite notInUse = setId(10, createStudySite(study, site1));
        StudySite inUse = setId(11, createStudySite(study, site2));
        inUse.getStudyParticipantAssignments().add(new StudyParticipantAssignment());

        siteDao.save(site1);
        studyDao.save(study);
        expectLastCall().anyTimes();
        authorizationManager.removeProtectionGroup(DomainObjectTools.createExternalObjectId(notInUse));
        replayMocks();

        try {
            service.removeTemplateFromSites(study, Arrays.asList(site1, site2));
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Cannot remove 1 site (Dartmouth) from study ECOG 1234 because there are participant(s) assigned", scve.getMessage());
        }
        verifyMocks();

        List<Site> remainingSites = study.getSites();
        assertEquals("Removable site not removed", 1, remainingSites.size());
        assertEquals("Wrong site retained", "Dartmouth", remainingSites.get(0).getName());
    }
}
