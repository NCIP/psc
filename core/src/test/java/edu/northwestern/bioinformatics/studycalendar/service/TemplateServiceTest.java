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
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;

import java.util.HashSet;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.EasyMock.expect;

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
        daoFinder = registerMockFor(DaoFinder.class);
        domainObjectDao = registerMockFor(DeletableDomainObjectDao.class);

        service = new TemplateService();
        service.setDeltaDao(deltaDao);
        service.setUserRoleDao(userRoleDao);
        service.setStudyDao(studyDao);
        service.setDaoFinder(daoFinder);

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
}
