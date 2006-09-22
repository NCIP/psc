package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;

import java.util.Date;
import java.util.List;

/**
 * @author Padmaja Vedula
 */
public class ParticipantDaoTest extends ContextDaoTestCase<ParticipantDao> {

    public void testGetAll() throws Exception {
        List<Participant> actual = getDao().getAll();
        assertEquals("Wrong size", new Integer(1), new Integer(actual.size()));
        Participant participant = actual.get(0);
        assertEquals("Wrong last name", "Scott", participant.getLastName());
    }

    public void testGetById() throws Exception {
        Participant participant = getDao().getById(-100);
        assertNotNull("Participant not found", participant);
        assertEquals("Wrong last name", "Scott", participant.getLastName());
    }

    public void testSaveStudyPartAssignments() throws Exception {
        Integer savedId;
        SiteDao siteDao = (SiteDao) getApplicationContext().getBean("siteDao");
        Site site = siteDao.getById(-1001);
        Participant participant = getDao().getById(-100);

        StudyParticipantAssignment spa = new StudyParticipantAssignment();
        spa.setParticipant(participant);
        spa.setStudySite(site.getStudySites().get(0));
        spa.setStartDateEpoch(new Date());

        participant.addStudyAssignment(spa);

        getDao().save(participant);
        savedId = participant.getId();

        interruptSession();

        Participant loaded = getDao().getById(savedId);
        assertNotNull("The saved participant id doesnt match" + new Integer(-100), loaded);
        assertEquals("Wrong study site", "study_identifier1", loaded.getStudyParticipantAssignments().get(0).getStudySite().getStudyIdentifier());
     }


    public void testSaveNewParticipant() throws Exception {
        Integer savedId;
        {
            Participant participant = new Participant();
            participant.setFirstName("Jeff");
            participant.setLastName("Someone");
            participant.setGender("Male");
            participant.setDateOfBirth(new Date());
            participant.setPersonId("123-45-6789");

            getDao().save(participant);
            savedId = participant.getId();
            assertNotNull("The saved participant id", savedId);
        }

        interruptSession();

        {
            Participant loaded = getDao().getById(savedId);
            assertNotNull("Could not reload participant id " + savedId, loaded);
            assertEquals("Wrong firstname", "Jeff", loaded.getFirstName());
            assertEquals("Wrong lastname", "Someone", loaded.getLastName());
            assertEquals("Wrong gender", "Male", loaded.getGender());

        }
    }

}