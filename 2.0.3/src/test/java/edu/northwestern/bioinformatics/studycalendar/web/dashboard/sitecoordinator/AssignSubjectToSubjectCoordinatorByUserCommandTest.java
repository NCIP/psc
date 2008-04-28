package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAssignment;

import static java.util.Arrays.asList;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSubject;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;

import java.util.List;

public class AssignSubjectToSubjectCoordinatorByUserCommandTest extends StudyCalendarTestCase {
    private AssignSubjectToSubjectCoordinatorByUserCommand command;
    private Subject subject0, subject1;
    private User pcNew;
    private SubjectDao subjectDao;
    private List<Subject> subjects;
    private Study study;
    private Site site;
    private User pcOld;

    protected void setUp() throws Exception {
        super.setUp();

        subjectDao = registerDaoMockFor(SubjectDao.class);

        command = new AssignSubjectToSubjectCoordinatorByUserCommand();
        command.setSubjectDao(subjectDao);

        pcOld = createNamedInstance("Old Subject Coordinator", User.class);
        pcNew = createNamedInstance("New Subject Coordinator", User.class);

        subject0 = createSubject("John", "Smith");
        subject1 = createSubject("Jake", "Smith");
        subjects = asList(subject0, subject1);

        study = createNamedInstance("Study A", Study.class);
        site = createNamedInstance("Northwestern", Site.class);

        StudySubjectAssignment assignment0 = createAssignment(study, site, subject0);
        StudySubjectAssignment assignment1 = createAssignment(study, site, subject1);

        assignment0.setSubjectCoordinator(pcOld);
        assignment1.setSubjectCoordinator(pcOld);

        subject0.addAssignment(assignment0);
        subject1.addAssignment(assignment1);


    }

    public void testAssignSubjectsToSubjectCoordinator() {
        command.setSubjects(subjects);
        command.setSubjectCoordinator(pcNew);
        command.setStudy(study);
        command.setSite(site);

        subjectDao.save(subject0);
        subjectDao.save(subject1);
        replayMocks();
        
        command.assignSubjectsToSubjectCoordinator();
        verifyMocks();

        assertEquals("Wrong number of assignments", 1, subject0.getAssignments().size());
        assertEquals("Wrong number of assignments", 1, subject1.getAssignments().size());

        assertEquals("Wrong subject coordinator assigned", pcNew.getName(), subject0.getAssignments().get(0).getSubjectCoordinator().getName());
        assertEquals("Wrong subject coordinator assigned", pcNew.getName(), subject1.getAssignments().get(0).getSubjectCoordinator().getName());
    }
}
