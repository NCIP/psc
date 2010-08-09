package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;

import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static java.util.Arrays.asList;

public class AssignSubjectToSubjectCoordinatorByUserCommandTest extends StudyCalendarTestCase {
    private AssignSubjectToSubjectCoordinatorByUserCommand command;
    private Subject subject0, subject1;
    private PscUser pcOld, pcNew;
    private SubjectDao subjectDao;
    private List<Subject> subjects;
    private Study study;
    private Site site;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        subjectDao = registerDaoMockFor(SubjectDao.class);

        command = new AssignSubjectToSubjectCoordinatorByUserCommand();
        command.setSubjectDao(subjectDao);

        pcOld = AuthorizationObjectFactory.createPscUser("Old Subject Coordinator", 15L);
        pcNew = AuthorizationObjectFactory.createPscUser("New Subject Coordinator", 50L);

        subject0 = createSubject("John", "Smith");
        subject1 = createSubject("Jake", "Smith");
        subjects = asList(subject0, subject1);

        study = createNamedInstance("Study A", Study.class);
        site = createNamedInstance("Northwestern", Site.class);

        StudySubjectAssignment assignment0 = createAssignment(study, site, subject0);
        StudySubjectAssignment assignment1 = createAssignment(study, site, subject1);

        assignment0.setStudySubjectCalendarManager(pcOld.getCsmUser());
        assignment1.setStudySubjectCalendarManager(pcOld.getCsmUser());
    }

    public void testAssignSubjectsToSubjectCoordinator() {
        command.setSubjects(subjects);
        command.setManagerCsmUserId(pcNew.getCsmUser().getUserId().intValue());
        command.setStudy(study);
        command.setSite(site);

        subjectDao.save(subject0);
        subjectDao.save(subject1);
        replayMocks();
        
        command.assignSubjectsToSubjectCoordinator();
        verifyMocks();

        assertEquals("Wrong number of assignments for John", 1, subject0.getAssignments().size());
        assertEquals("Wrong number of assignments for Jake", 1, subject1.getAssignments().size());

        assertEquals("Wrong subject coordinator assigned", pcNew.getCsmUser().getUserId().intValue(),
            subject0.getAssignments().get(0).getManagerCsmUserId().intValue());
        assertEquals("Wrong subject coordinator assigned", pcNew.getCsmUser().getUserId().intValue(),
            subject1.getAssignments().get(0).getManagerCsmUserId().intValue());
    }
}
