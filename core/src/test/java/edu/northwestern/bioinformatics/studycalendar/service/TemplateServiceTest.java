package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.DeletableDomainObjectDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.ReleasedTemplate;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.TemplateAvailability;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings.*;
import static org.easymock.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class TemplateServiceTest extends StudyCalendarTestCase {
    private TemplateService service;

    private DaoFinder daoFinder;
    private DeltaDao deltaDao;
    private UserRoleDao userRoleDao;
    private StudyDao studyDao;

    private DeletableDomainObjectDao domainObjectDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        deltaDao = registerDaoMockFor(DeltaDao.class);
        userRoleDao = registerDaoMockFor(UserRoleDao.class);
        studyDao = registerMockFor(StudyDao.class);
        AuthorizationService authorizationService = registerMockFor(AuthorizationService.class);
        daoFinder = registerMockFor(DaoFinder.class);
        domainObjectDao = registerMockFor(DeletableDomainObjectDao.class);

        service = new TemplateService();
        service.setDeltaDao(deltaDao);
        service.setUserRoleDao(userRoleDao);
        service.setStudyDao(studyDao);
        service.setDaoFinder(daoFinder);
        service.setAuthorizationService(authorizationService);

        createUser("jimbo", Role.SITE_COORDINATOR, Role.SUBJECT_COORDINATOR);
    }

    public void testAssignTemplateToSubjectCoordinatorRequiresSite() throws Exception {
        try {
            service.assignTemplateToSubjectCoordinator(null, null, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of studyTest ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.STUDY_IS_NULL, ise.getMessage());
        }
    }

    public void testAssignTemplateToSubjectCoordinatorRequiresStudy() throws Exception {
        Study study = createNamedInstance("sldfksdfjk", Study.class);
        try {
            service.assignTemplateToSubjectCoordinator(study, null, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of siteTest ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.SITE_IS_NULL, ise.getMessage());
        }
    }

    public void testAssignTemplateToSubjectCoordinatorRequiresUser() throws Exception {
        Study study = createNamedInstance("sldfksdfjk", Study.class);
        Site  site  = createNamedInstance("asdf", Site.class);
        try {
            service.assignTemplateToSubjectCoordinator(study, site, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of userTest ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.USER_IS_NULL, ise.getMessage());
        }
    }

    public void testComparatorForStudyName() throws Exception {
        Study a = createNamedInstance("a", Study.class);
        Study aa = createNamedInstance("aa", Study.class);
        Study A = createNamedInstance("A", Study.class);
        Study AA = createNamedInstance("AA", Study.class);
        Study ab = createNamedInstance("ab", Study.class);
        Study one = createNamedInstance("1", Study.class);
        Study underscoreOne = createNamedInstance("_1", Study.class);
        Study y = createNamedInstance("y", Study.class);
        Study YY = createNamedInstance("YY", Study.class);
        Study m = createNamedInstance("m", Study.class);
        Study MM = createNamedInstance("MM", Study.class);
        Study asdf = createNamedInstance("asdf", Study.class);

        Amendment amendment = createAmendments("a1", "a2", "a3");
        a.setAmendment(amendment);
        aa.setAmendment(amendment);
        A.setAmendment(amendment);
        AA.setAmendment(amendment);
        ab.setAmendment(amendment);
        one.setAmendment(amendment);
        underscoreOne.setAmendment(amendment);
        y.setAmendment(amendment);
        YY.setAmendment(amendment);
        m.setAmendment(amendment);
        MM.setAmendment(amendment);
        asdf.setAmendment(amendment);

        List<Study> allStudies = new ArrayList<Study>();
        allStudies.add(MM);
        allStudies.add(a);
        allStudies.add(ab);
        allStudies.add(one);
        allStudies.add(underscoreOne);
        allStudies.add(y);
        allStudies.add(aa);
        allStudies.add(A);
        allStudies.add(AA);
        allStudies.add(YY);
        allStudies.add(m);
        allStudies.add(asdf);

        List<ReleasedTemplate> releasedTemplates = new ArrayList<ReleasedTemplate>();
        for (Study visibleStudy : allStudies) {
            if (visibleStudy.isReleased()) {
                releasedTemplates.add(new ReleasedTemplate(visibleStudy, true));
            }
        }

        assertEquals("Wrong study before sorting", MM, releasedTemplates.get(0).getStudy());
        assertEquals("Wrong study before sorting", a, releasedTemplates.get(1).getStudy());
        assertEquals("Wrong study before sorting", ab, releasedTemplates.get(2).getStudy());
        assertEquals("Wrong study before sorting", one, releasedTemplates.get(3).getStudy());
        assertEquals("Wrong study before sorting", underscoreOne, releasedTemplates.get(4).getStudy());
        assertEquals("Wrong study before sorting", y, releasedTemplates.get(5).getStudy());
        assertEquals("Wrong study before sorting", aa, releasedTemplates.get(6).getStudy());
        assertEquals("Wrong study before sorting", A, releasedTemplates.get(7).getStudy());
        assertEquals("Wrong study before sorting", AA, releasedTemplates.get(8).getStudy());
        assertEquals("Wrong study before sorting", YY, releasedTemplates.get(9).getStudy());
        assertEquals("Wrong study before sorting", m, releasedTemplates.get(10).getStudy());

        Collections.sort(releasedTemplates, TemplateService.AlphabeticallyOrderedComparator.INSTANCE);

        assertEquals("Wrong study after sorting", one, releasedTemplates.get(0).getStudy());
        assertEquals("Wrong study after sorting", underscoreOne, releasedTemplates.get(1).getStudy());
        assertEquals("Wrong study after sorting", a, releasedTemplates.get(2).getStudy());
        assertEquals("Wrong study after sorting", A, releasedTemplates.get(3).getStudy());
        assertEquals("Wrong study after sorting", aa, releasedTemplates.get(4).getStudy());
        assertEquals("Wrong study after sorting", AA, releasedTemplates.get(5).getStudy());
        assertEquals("Wrong study after sorting", ab, releasedTemplates.get(6).getStudy());
        assertEquals("Wrong study after sorting", asdf, releasedTemplates.get(7).getStudy());
        assertEquals("Wrong study after sorting", m, releasedTemplates.get(8).getStudy());
        assertEquals("Wrong study after sorting", MM, releasedTemplates.get(9).getStudy());
        assertEquals("Wrong study after sorting", y, releasedTemplates.get(10).getStudy());
    }

    public void testRemoveAssignedTemplateFromSubjectCoordinatorRequiresSite() throws Exception {
        try {
            service.removeAssignedTemplateFromSubjectCoordinator(null, null, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of studyTest ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.STUDY_IS_NULL, ise.getMessage());
        }
    }

    public void testRemoveAssignedTemplateFromSubjectCoordinatorRequiresStudy() throws Exception {
        Study study = createNamedInstance("sldfksdfjk", Study.class);
        try {
            service.removeAssignedTemplateFromSubjectCoordinator(study, null, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of siteTest ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.SITE_IS_NULL, ise.getMessage());
        }
    }

    public void testRemoveAssignedTemplateFromSubjectCoordinatorRequiresUser() throws Exception {
        Study study = createNamedInstance("sldfksdfjk", Study.class);
        Site  site  = createNamedInstance("asdf", Site.class);
        try {
            service.removeAssignedTemplateFromSubjectCoordinator(study, site, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of userTest ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.USER_IS_NULL, ise.getMessage());
        }
    }

    public void testAssignTemplateToSubjectCoordinator() throws Exception {
        Site  site          = setId(1, createNamedInstance("Northwestern", Site.class));
        Study study         = setId(1, createNamedInstance("Study A", Study.class));
        setId(1, createStudySite(study, site));

        edu.northwestern.bioinformatics.studycalendar.domain.User user =
                setId(1, createNamedInstance("John", edu.northwestern.bioinformatics.studycalendar.domain.User.class));
        user.setCsmUserId(1L);

        UserRole userRole = createUserRole(user, Role.SUBJECT_COORDINATOR, site);
        user.addUserRole(userRole);

        userRoleDao.save(userRole);
        replayMocks();

        edu.northwestern.bioinformatics.studycalendar.domain.User actualUser =
                service.assignTemplateToSubjectCoordinator(study, site, user);
        verifyMocks();

        UserRole actualUserRole = actualUser.getUserRole(Role.SUBJECT_COORDINATOR);
        assertEquals("Wrong study site size", 1, actualUserRole.getStudySites().size());
    }

    public void testAssignTemplateToSubjectCoordinatorStudySiteAlreadyExists() throws Exception {
        Site  site          = setId(1, createNamedInstance("Northwestern", Site.class));
        Study study         = setId(1, createNamedInstance("Study A", Study.class));
        StudySite studySite = setId(1, createStudySite(study, site));

        edu.northwestern.bioinformatics.studycalendar.domain.User user =
                setId(1, createNamedInstance("John", edu.northwestern.bioinformatics.studycalendar.domain.User.class));
        user.setCsmUserId(1L);

        UserRole userRole = createUserRole(user, Role.SUBJECT_COORDINATOR, site);
        user.addUserRole(userRole);
        userRole.addStudySite(studySite);

        replayMocks();

        edu.northwestern.bioinformatics.studycalendar.domain.User actualUser =
                service.assignTemplateToSubjectCoordinator(study, site, user);
        verifyMocks();

        UserRole actualUserRole = actualUser.getUserRole(Role.SUBJECT_COORDINATOR);
        assertEquals("Wrong study site size", 1, actualUserRole.getStudySites().size());
    }

    public void testRemoveAssignedTemplateFromSubjectCoordinator() throws Exception {
        Site site0  = setId(1, createNamedInstance("Northwestern", Site.class));
        Site site1  = setId(2, createNamedInstance("Mayo", Site.class));
        Study study = setId(1, createNamedInstance("Study A", Study.class));

        StudySite studySite0 = setId(1, createStudySite(study, site0));
        StudySite studySite1 = setId(2, createStudySite(study, site1));

        edu.northwestern.bioinformatics.studycalendar.domain.User user =
                setId(1, createNamedInstance("John", edu.northwestern.bioinformatics.studycalendar.domain.User.class));
        user.setCsmUserId(1L);

        UserRole userRole = createUserRole(user, Role.SUBJECT_COORDINATOR, site0, site1);
        userRole.addStudySite(studySite0);
        userRole.addStudySite(studySite1);

        user.addUserRole(userRole);

        userRoleDao.save(userRole);

        String studySitePGName = DomainObjectTools.createExternalObjectId(studySite0);
        ProtectionGroup expectedPG = createProtectionGroup(1L, studySitePGName);
        replayMocks();

        edu.northwestern.bioinformatics.studycalendar.domain.User actualUser
                = service.removeAssignedTemplateFromSubjectCoordinator(study, site0, user);
        verifyMocks();

        UserRole actualUserRole = actualUser.getUserRole(Role.SUBJECT_COORDINATOR);
        assertEquals("Wrong study site0 size", 1, actualUserRole.getStudySites().size());
        assertEquals("Wrong study site", studySite1, actualUserRole.getStudySites().get(0));
    }

    public void testRemoveAssignedTemplateFromSubjectCoordinatorWhenSubjCoordHasAssignments() throws Exception {
        Site nu = setId(1, createNamedInstance("Northwestern", Site.class));
        Site mayo = setId(2, createNamedInstance("Mayo", Site.class));
        Study study = setId(1, createNamedInstance("Study A", Study.class));

        StudySite nuSS = setId(1, createStudySite(study, nu));
        StudySite mayoSS = setId(2, createStudySite(study, mayo));

        StudySubjectAssignment assignment = createAssignment(study, nu, createSubject("Don't", "Care"));

        edu.northwestern.bioinformatics.studycalendar.domain.User subjectCoordinator =
                setId(1, createUser("jimbo", Role.SUBJECT_COORDINATOR));
        subjectCoordinator.setCsmUserId(1L);
        subjectCoordinator.getStudySubjectAssignments().add(assignment);

        UserRole userRole = subjectCoordinator.getUserRole(Role.SUBJECT_COORDINATOR);
        userRole.addStudySite(nuSS);
        userRole.addStudySite(mayoSS);

        replayMocks();
        try {
            service.removeAssignedTemplateFromSubjectCoordinator(study, nu, subjectCoordinator);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Wrong exception message",
                "jimbo is still responsible for one or more subjects on Study A at Northwestern.  Please reassign those subjects before removing jimbo from that study and site.",
                scve.getMessage());
        }
    }

    public void testFindParentWhenImmediatelyAvailable() throws Exception {
        Study study = createBasicTemplate();
        assertSame(study.getPlannedCalendar(),
            service.findParent(study.getPlannedCalendar().getEpochs().get(1)));
        assertSame(study.getPlannedCalendar().getEpochs().get(1),
            service.findParent(study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0)));
    }

    public void testFindParentWhenNotImmediatelyAvailable() throws Exception {
        Study study = createBasicTemplate();
        Epoch e1 = study.getPlannedCalendar().getEpochs().get(1);
        StudySegment e1a0 = e1.getStudySegments().get(0);
        e1a0.setParent(null);
        e1.getStudySegments().remove(e1a0);

        expect(deltaDao.findDeltaWhereAdded(e1a0)).andReturn(Delta.createDeltaFor(e1));
        replayMocks();

        assertSame(e1, service.findParent(e1a0));
        verifyMocks();
    }

    public void testFindParentWhenAddedAsChildOfAnotherAddAndThenRemovedAlone() throws Exception {
        // simulates this process:
        //   Period P0 with child P0E0 added to E1A1 --> Results in Add(P0) in amendment R0
        //   P0E0 removed in amendment R1 --> Results in  Remove(P0E0) and P0E0.period=>null

        Study study = createBasicTemplate();
        Period p = createPeriod("P0", 3, 17, 1);
        PlannedActivity p0e0 = createPlannedActivity("P0E0", 4);
        study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(1).addPeriod(p);

        expect(deltaDao.findDeltaWhereAdded(p0e0)).andReturn(null);
        expect(deltaDao.findDeltaWhereRemoved(p0e0)).andReturn(Delta.createDeltaFor(p, Remove.create(p0e0)));
        replayMocks();

        assertSame(p, service.findParent(p0e0));
        verifyMocks();
    }

    public void testDelete() throws Exception {
        PlannedActivity pa = createPlannedActivity("P0E0", 4);
        PlannedActivityLabel pal = createPlannedActivityLabel("label");
        pa.addPlannedActivityLabel(pal);

        expect(daoFinder.findDao(PlannedActivity.class)).andReturn(domainObjectDao);
        expect(daoFinder.findDao(PlannedActivityLabel.class)).andReturn(domainObjectDao);
        domainObjectDao.delete(pal);
        domainObjectDao.delete(pa);

        replayMocks();
        service.delete(pa);
        verifyMocks();
    }

    public void testFindAncestorWhenPossible() throws Exception {
        Study study = createBasicTemplate();
        Epoch e1 = study.getPlannedCalendar().getEpochs().get(1);
        StudySegment e1a0 = e1.getStudySegments().get(0);

        assertEquals(e1, service.findAncestor(e1a0, Epoch.class));
        assertEquals(study.getPlannedCalendar(), service.findAncestor(e1a0, PlannedCalendar.class));
        assertEquals(study.getPlannedCalendar(), service.findAncestor(e1, PlannedCalendar.class));
    }

    public void testFindAncestorWhenDynamicSubclass() throws Exception {
        Study study = createBasicTemplate();
        Epoch dynamic = new Epoch() { };
        study.getPlannedCalendar().addEpoch(dynamic);

        assertSame(study.getPlannedCalendar(), service.findAncestor(dynamic, PlannedCalendar.class));
    }

    public void testFindAncestorWhenNotPossible() throws Exception {
        Study study = createBasicTemplate();
        Epoch e1 = study.getPlannedCalendar().getEpochs().get(1);

        try {
            service.findAncestor(e1, Period.class);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException expected) {
            assertEquals("Epoch is not a descendant of Period", expected.getMessage());
        }
    }

    public void testFindStudyForPlannedCalendar() throws Exception {
        Study study = createBasicTemplate();
        assertSame(study, service.findStudy(study.getPlannedCalendar()));
    }

    public void testFindStudyForOtherNodes() throws Exception {
        Study study = createBasicTemplate();
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
        assertSame(study, service.findStudy(epoch));
        assertSame(study, service.findStudy(epoch.getStudySegments().get(0)));
    }

    public void testFindEquivalentChildForStudyNode() throws Exception {
        Study study = createBasicTemplate();
        assignIds(study);
        Study studyAsNode = study;
        assertSame(study, service.findEquivalentChild(study, studyAsNode));
    }

    public void testFindEquivalentChildForPopulationNode() throws Exception {
        Study study = createBasicTemplate();
        assignIds(study);
        Population population = new Population();
        population.setName("population name");
        population.setAbbreviation("population abbreviation");
        Set<Population> populations = new HashSet<Population>();
        populations.add(population);
        study.setPopulations(populations);
        assertSame(population, service.findEquivalentChild(study, population));
    }

    public void testFindEquivalentChildWhenActualChild() throws Exception {
        Study study = createBasicTemplate();
        assignIds(study);
        Epoch e = study.getPlannedCalendar().getEpochs().get(1);
        assertSame(e, service.findEquivalentChild(study, e));
    }

    public void testFindEquivalentChildByIdAndType() throws Exception {
        int sameId = 50;
        PlannedActivity expectedNode = setId(sameId, createPlannedActivity("PA0", 3));

        Study study = createBasicTemplate();
        assignIds(study);
        Period p0 = setId(sameId, createPeriod("P0", 1, 14, 4));
        p0.addPlannedActivity(setId(49, createPlannedActivity("PA1", 1)));
        p0.addPlannedActivity(expectedNode);
        study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0).addPeriod(p0);

        PlannedActivity parameter = setId(sameId, new PlannedActivity());
        // set one of each node to the same id to ensure that type checking is happening
        study.getPlannedCalendar().setId(sameId);
        study.getPlannedCalendar().getEpochs().get(0).setId(sameId);
        study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0).setId(sameId);

        assertSame(expectedNode, service.findEquivalentChild(study, parameter));
    }

    public void testFindEquivalentChildByGridIdAndType() throws Exception {
        String sameGridId = "gridId50";
        PlannedActivity expectedNode = setGridId(sameGridId, createPlannedActivity("PA0", 3));

        Study study = createBasicTemplate();
        assignIds(study);
        Period p0 = setGridId(sameGridId, createPeriod("P0", 1, 14, 4));
        p0.addPlannedActivity(setGridId("gridId49", createPlannedActivity("PA1", 1)));
        p0.addPlannedActivity(expectedNode);
        study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0).addPeriod(p0);

        PlannedActivity parameter = setGridId(sameGridId, new PlannedActivity());
        // set one of each node to the same id to ensure that type checking is happening
        study.getPlannedCalendar().setGridId(sameGridId);
        study.getPlannedCalendar().getEpochs().get(0).setGridId(sameGridId);
        study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0).setGridId(sameGridId);

        assertSame(expectedNode, service.findEquivalentChild(study, parameter));
    }

    public void testGetVisibleTemplates() throws Exception {
        Site nu = createSite("NU", "IL090");
        Study readyAndInDev = createBasicTemplate("R");
        StudySite nuR = readyAndInDev.addSite(nu);
        nuR.approveAmendment(readyAndInDev.getAmendment(), new Date());
        readyAndInDev.setDevelopmentAmendment(new Amendment());

        Study pending = createBasicTemplate("P");
        Study inDev = createInDevelopmentBasicTemplate("D");

        PscUser user = AuthorizationObjectFactory.createPscUser("jo",
            createSuiteRoleMembership(PscRole.STUDY_QA_MANAGER).forAllSites(),
            createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies());

        expect(studyDao.searchVisibleStudies(user.getVisibleStudyParameters(), null)).
            andReturn(Arrays.asList(pending, readyAndInDev, inDev));

        replayMocks();
        Map<TemplateAvailability, List<UserTemplateRelationship>> actual
            = service.getVisibleTemplates(user);
        verifyMocks();

        System.out.println(actual);

        List<UserTemplateRelationship> actualPending = actual.get(TemplateAvailability.PENDING);
        assertEquals("Wrong number of pending templates", 1, actualPending.size());
        assertEquals("Wrong pending template", "P", actualPending.get(0).getStudy().getAssignedIdentifier());

        List<UserTemplateRelationship> actualAvailable = actual.get(TemplateAvailability.AVAILABLE);
        assertEquals("Wrong number of available templates", 1, actualAvailable.size());
        assertEquals("Wrong available template", "R", actualAvailable.get(0).getStudy().getAssignedIdentifier());

        List<UserTemplateRelationship> actualDev = actual.get(TemplateAvailability.IN_DEVELOPMENT);
        assertEquals("Wrong number of dev templates", 2, actualDev.size());
        assertEquals("Wrong 1st dev template", "D", actualDev.get(0).getStudy().getAssignedIdentifier());
        assertEquals("Wrong 2nd dev template", "R", actualDev.get(1).getStudy().getAssignedIdentifier());
    }

    public void testSearchVisibleTemplates() throws Exception {
        Study inDev = createInDevelopmentBasicTemplate("D");

        PscUser user = AuthorizationObjectFactory.createPscUser("jo",
            createSuiteRoleMembership(PscRole.STUDY_QA_MANAGER).forAllSites(),
            createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies());

        expect(studyDao.searchVisibleStudies(user.getVisibleStudyParameters(), "d")).
            andReturn(Arrays.asList(inDev));

        replayMocks();
        Map<TemplateAvailability, List<UserTemplateRelationship>> actual
            = service.searchVisibleTemplates(user, "d");
        verifyMocks();

        System.out.println(actual);

        assertEquals("Wrong number of pending templates", 0, actual.get(TemplateAvailability.PENDING).size());
        assertEquals("Wrong number of available templates", 0, actual.get(TemplateAvailability.AVAILABLE).size());

        List<UserTemplateRelationship> actualDev = actual.get(TemplateAvailability.IN_DEVELOPMENT);
        assertEquals("Wrong number of dev templates", 1, actualDev.size());
        assertEquals("Wrong 1st dev template", "D", actualDev.get(0).getStudy().getAssignedIdentifier());
    }
}
