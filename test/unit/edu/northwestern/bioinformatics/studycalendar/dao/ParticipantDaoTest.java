package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.testing.ContextTools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import org.springframework.context.ApplicationContext;

/**
 * @author Rhett Sutphin
 */
public class ParticipantDaoTest extends DaoTestCase {
    private ParticipantDao dao = (ParticipantDao) getApplicationContext().getBean("participantDao");
    private static ApplicationContext applicationContext = null;
    

    public void testGetAll() throws Exception {

        List<Participant> actual = dao.getAll();
        
        System.out.println("Size is " + actual.size());
        
        for (int i=0;i<actual.size();i++) {
            Participant s1 = actual.get(i);
            System.out.println(s1.getLastName());
        }

    }

    public void testSaveStudyPartAssignments() throws Exception {
    	Integer savedId;
        StudyDao studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
        Study study = new Study();
        study.setName("New study");
        studyDao.save(study);
        
        SiteDao siteDao = (SiteDao) getApplicationContext().getBean("siteDao");
        Site site = new Site();
        site.setName("New Site");
        
        StudySite studySite = new StudySite();
        studySite.setSite(site);
        studySite.setStudy(study);
        studySite.setStudyIdentifier("study_identifier");
        ArrayList studySites = new ArrayList();
        studySites.add(studySite);
        site.setStudySites(studySites);
        siteDao.save(site);
        
        Participant participant = new Participant();
        participant.setLastName("Smith");
        participant.setFirstName("John");
        participant.setDateOfBirth(new Date());
        participant.setGender("Male");
        participant.setPersonId("276-99-8970");
        
        
        StudyParticipantAssignment spa = new StudyParticipantAssignment();
        spa.setParticipant(participant);
        spa.setStudy(study);
        spa.setStudySite(site.getStudySites().get(0));
        spa.setDateOfEnrollment(new Date());

        participant.addStudyParticipantAssignments(spa);
        
        dao.save(participant);
        dao.save(participant);
        savedId = participant.getId();
        assertNotNull("The saved participant id", savedId);
    
        interruptSession();

    
        Participant loaded = dao.getById(savedId);
        assertNotNull("Could not reload participant id " + savedId, loaded);
        assertEquals("Wrong study", "New study", loaded.getStudyParticipantAssignments().get(0).getStudy().getName());
 

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
            
            dao.save(participant);
            savedId = participant.getId();
            assertNotNull("The saved participant id", savedId);
        }

        interruptSession();

        {
            Participant loaded = dao.getById(savedId);
            assertNotNull("Could not reload participant id " + savedId, loaded);
            assertEquals("Wrong firstname", "Jeff", loaded.getFirstName());
            assertEquals("Wrong lastname", "Someone", loaded.getLastName());
            assertEquals("Wrong gender", "Male", loaded.getGender());
        
        }
    }

    
}