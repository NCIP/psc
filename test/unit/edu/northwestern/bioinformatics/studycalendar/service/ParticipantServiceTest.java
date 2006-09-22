package edu.northwestern.bioinformatics.studycalendar.service;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ParticipantServiceTest extends StudyCalendarTestCase {
    private ParticipantDao participantDao;
    private ParticipantService service;

    protected void setUp() throws Exception {
        super.setUp();
        participantDao = registerMockFor(ParticipantDao.class);

        service = new ParticipantService();
        service.setParticipantDao(participantDao);
    }

    public void testAssignParticipant() throws Exception {
        Study study = Fixtures.createNamedInstance("Glancing", Study.class);
        Site site = Fixtures.createNamedInstance("Lake", Site.class);
        StudySite studySite = Fixtures.createStudySite(study, site);
        Participant participantIn = Fixtures.createParticipant("Alice", "Childress");
        Date startDate = DateUtils.createDate(2006, Calendar.OCTOBER, 31);

        Participant participantExpectedSave = Fixtures.createParticipant("Alice", "Childress");

        StudyParticipantAssignment expectedAssignment = new StudyParticipantAssignment();
        expectedAssignment.setStartDateEpoch(startDate);
        expectedAssignment.setParticipant(participantExpectedSave);
        expectedAssignment.setStudySite(studySite);

        participantExpectedSave.addStudyAssignment(expectedAssignment);

        participantDao.save(participantExpectedSave);
        replayMocks();

        service.assignParticipant(participantIn, studySite, startDate);
        verifyMocks();

        assertEquals("Assignment not added to participant", 1, participantIn.getStudyParticipantAssignments().size());
    }
}
