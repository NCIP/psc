package edu.northwestern.bioinformatics.studycalendar.api.impl;

import static org.easymock.EasyMock.expect;
import org.easymock.classextension.EasyMock;
import org.easymock.IArgumentMatcher;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.WithBigId;
import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class DefaultPlannedCalendarServiceTest extends StudyCalendarTestCase {
    private DefaultPlannedCalendarService service;

    private StudyDao studyDao;
    private PlannedCalendarDao plannedCalendarDao;
    private SiteDao siteDao;
    private TemplateService templateService;

    private Study parameterStudy;
    private Study loadedStudy;

    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        plannedCalendarDao = registerDaoMockFor(PlannedCalendarDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        templateService = registerMockFor(TemplateService.class);

        service = new DefaultPlannedCalendarService();
        service.setStudyDao(studyDao);
        service.setPlannedCalendarDao(plannedCalendarDao);
        service.setSiteDao(siteDao);
        service.setTemplateService(templateService);

        parameterStudy = Fixtures.createNamedInstance("S1", Study.class);
        parameterStudy.setBigId("UNIQUE!");
        addSite(parameterStudy, 1);

        loadedStudy = Fixtures.createNamedInstance("S1", Study.class);
        loadedStudy.setBigId("UNIQUE!");
        loadedStudy.setPlannedCalendar(new PlannedCalendar());
        addSite(loadedStudy, 1);
    }

    private void addSite(Study target, int n) {
        Site site = Fixtures.createNamedInstance("Site " + n, Site.class);
        site.setBigId("SITE" + n + "-GUID");
        Fixtures.createStudySite(target, site);
    }

    public void testRegisterRequiresBigId() throws Exception {
        parameterStudy.setBigId(null);
        assertRegistrationError("study missing bigId");
    }

    public void testRegisterRequiresName() throws Exception {
        parameterStudy.setName(null);
        assertRegistrationError("study missing name");
    }

    public void testRegisterRequiresSiteBigId() throws Exception {
        parameterStudy.getStudySites().get(0).getSite().setBigId(null);
        expect(studyDao.getByBigId(parameterStudy.getBigId())).andReturn(null);
        assertRegistrationError("site missing bigId");
    }

    public void testRegisterNewSiteRequiresSiteName() throws Exception {
        parameterStudy.getStudySites().get(0).getSite().setName(null);
        expect(studyDao.getByBigId(parameterStudy.getBigId())).andReturn(null);
        expect(siteDao.getByBigId(parameterStudy.getStudySites().get(0).getSite().getBigId())).andReturn(null);
        assertRegistrationError("new site missing name");
    }

    public void testRegisterExistingSiteDoesNotRequireSiteName() throws Exception {
        parameterStudy.getStudySites().get(0).getSite().setName(null);
        expect(siteDao.getByBigId(parameterStudy.getSites().get(0).getBigId()))
            .andReturn(loadedStudy.getSites().get(0));
        expectBasicGet();
        studyDao.save(loadedStudy);

        replayMocks();
        service.registerStudy(parameterStudy);
        verifyMocks();
    }

    public void testDefaultDefaultCreatorIsBasic() throws Exception {
        assertSame(TemplateSkeletonCreator.BASIC, service.getDefaultTemplateCreator());
    }

    public void testRegisterUsesSkeletonCreator() throws Exception {
        Study created = new Study();
        created.setPlannedCalendar(new PlannedCalendar());
        TemplateSkeletonCreator defaultCreator = registerMockFor(TemplateSkeletonCreator.class);
        service.setDefaultTemplateCreator(defaultCreator);

        expect(studyDao.getByBigId(parameterStudy.getBigId())).andReturn(null);
        expect(defaultCreator.create()).andReturn(created);
        expect(siteDao.getByBigId(parameterStudy.getSites().get(0).getBigId()))
            .andReturn(loadedStudy.getSites().get(0));
        studyDao.save(created);
        replayMocks();
        PlannedCalendar actual = service.registerStudy(parameterStudy);
        verifyMocks();

        assertSame(created.getPlannedCalendar(), actual);
    }

    public void testBasicNewRegistration() throws Exception {
        expect(siteDao.getByBigId(parameterStudy.getSites().get(0).getBigId()))
            .andReturn(loadedStudy.getSites().get(0));
        Study actualStudy = expectRegisterMain(null);
        assertEquals("sites not copied to created study", parameterStudy.getStudySites().size(), actualStudy.getStudySites().size());
        for (int i = 0; i < parameterStudy.getStudySites().size(); i++) {
            assertStudySitesSameSite("Incorrect study site copied to created study",
                parameterStudy.getStudySites().get(i), actualStudy.getStudySites().get(i));
        }
    }
    
    public void testNewRegistrationWithPlannedCalendar() throws Exception {
        PlannedCalendar expectedPlannedCalendar = new PlannedCalendar();
        parameterStudy.setPlannedCalendar(expectedPlannedCalendar);

        expect(siteDao.getByBigId(parameterStudy.getSites().get(0).getBigId()))
            .andReturn(loadedStudy.getSites().get(0));

        PlannedCalendar actual = expectRegisterMain(null).getPlannedCalendar();

        assertSame(expectedPlannedCalendar, actual);
    }

    public void testReregisterWithNewSite() throws Exception {
        addSite(parameterStudy, 2);
        expect(siteDao.getByBigId(parameterStudy.getSites().get(0).getBigId()))
            .andReturn(loadedStudy.getSites().get(0));
        Site newSite = parameterStudy.getSites().get(1);
        expect(siteDao.getByBigId(newSite.getBigId())).andReturn(null);
        siteDao.save(newSite);

        Study actualStudy = expectRegisterMain(loadedStudy);
        assertEquals("new site not add to created study", parameterStudy.getStudySites().size(), actualStudy.getStudySites().size());
        for (int i = 0; i < parameterStudy.getStudySites().size(); i++) {
            assertStudySitesSameSite("Incorrect study site copied to created study",
                parameterStudy.getStudySites().get(i), actualStudy.getStudySites().get(i));
        }
    }

    public void testReregisterRemovingUnusedSite() throws Exception {
        addSite(loadedStudy, 2);
        expect(siteDao.getByBigId(parameterStudy.getSites().get(0).getBigId()))
            .andReturn(loadedStudy.getSites().get(0));
        templateService.removeTemplateFromSites(loadedStudy, Arrays.asList(loadedStudy.getSites().get(1)));
        expectRegisterMain(loadedStudy);
        // mock verification is sufficient
    }
    
    private Study expectRegisterMain(Study existing) {
        if (existing == null) {
            expect(studyDao.getByBigId(parameterStudy.getBigId())).andReturn(null);
        } else {
            expectBasicGet();
        }
        expectSaveStudy();

        replayMocks();
        PlannedCalendar actual = service.registerStudy(parameterStudy);
        verifyMocks();

        return actual.getStudy();
    }

    public void testRegisterReturnsExistingIfAlreadyRegistered() throws Exception {
        expect(siteDao.getByBigId(parameterStudy.getSites().get(0).getBigId()))
            .andReturn(loadedStudy.getSites().get(0));
        expectBasicGet();
        studyDao.save(loadedStudy);
        replayMocks();

        assertSame(loadedStudy.getPlannedCalendar(), service.registerStudy(parameterStudy));
    }

    public void testNullIfNoStudy() throws Exception {
        expect(studyDao.getByBigId(parameterStudy.getBigId())).andReturn(null);
        replayMocks();

        assertNull(service.getPlannedCalendar(parameterStudy));
    }

    public void testGetWithoutBigId() throws Exception {
        parameterStudy.setBigId(null);
        try {
            service.getPlannedCalendar(parameterStudy);
        } catch (IllegalArgumentException iae) {
            assertEquals("Cannot locate planned calendar for a study without a bigId", iae.getMessage());
        }
    }

    public void testBasicGet() throws Exception {
        expectBasicGet();
        replayMocks();

        assertSame(loadedStudy.getPlannedCalendar(), service.getPlannedCalendar(parameterStudy));
    }

    private void expectBasicGet() {
        expect(studyDao.getByBigId(parameterStudy.getBigId())).andReturn(loadedStudy);
        plannedCalendarDao.initialize(loadedStudy.getPlannedCalendar());
    }

    private void expectSaveStudy() {
        EasyMock.reportMatcher(new IArgumentMatcher() {
            public boolean matches(Object argument) {
                assertEquals("Mismatched bigIds", parameterStudy.getBigId(), ((WithBigId) argument).getBigId());
                assertEquals("Mismatched names", parameterStudy.getName(), ((Named) argument).getName());
                return true;
            }

            public void appendTo(StringBuffer buffer) {
                buffer.append(" (by bigId)");
            }
        });
        studyDao.save(null);
    }

    private void assertRegistrationError(String submsg) {
        replayMocks();
        try {
            service.registerStudy(parameterStudy);
        } catch (IllegalArgumentException iae) {
            assertEquals("Wrong error message", "Cannot register study: " + submsg, iae.getMessage());
        }
    }

    private static void assertStudySitesSameSite(String message, StudySite expected, StudySite actual) {
        assertEquals(message + ": site bigIds differ", expected.getSite().getBigId(), actual.getSite().getBigId());
        assertEquals(message + ": site names differ", expected.getSite().getName(), actual.getSite().getName());
    }
}
