/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEventValue;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;

/**
 * @author Jalpa Patel
 */
public class AuditCollectionUpdateListenerIntegratedTest extends AuditEventListenerTestCase {
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private PopulationDao populationDao;
    private SubjectDao subjectDao;
    private ScheduledActivityDao scheduledActivityDao;

    @Override
    protected String getTestDataFileName() {
        return String.format("testdata/%s.xml",
            AuditCollectionUpdateListenerIntegratedTest.class.getSimpleName());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studySubjectAssignmentDao = (StudySubjectAssignmentDao) getApplicationContext().getBean("studySubjectAssignmentDao");
        populationDao = (PopulationDao) getApplicationContext().getBean("populationDao");
        subjectDao = (SubjectDao) getApplicationContext().getBean("subjectDao");
        scheduledActivityDao = (ScheduledActivityDao) getApplicationContext().getBean("scheduledActivityDao");
    }

    public void testAuditEventAfterStudySubjectAssignmentPopulationUpdate() throws Exception {
        {
            StudySubjectAssignment loaded = studySubjectAssignmentDao.getById(-10);
            Population population = populationDao.getById(-20);
            loaded.addPopulation(population);
            studySubjectAssignmentDao.save(loaded);
        }

        interruptSession();
        int eventId = getEventIdForObject(-10, StudySubjectAssignment.class.getName(), Operation.UPDATE.name(), "populations");
        assertNotNull("Audit event for UPDATE is not created ", eventId);

        DataAuditEventValue populationsValue = getAuditEventValueFor(eventId, "populations");

        // Testing populations value
        assertNotNull("No data audit event value for populations", populationsValue);
        assertEquals("Populations previous value doesn't match", "-21", populationsValue.getPreviousValue());
        assertTrue("Populations current value doesn't match", populationsValue.getCurrentValue().contains("-20"));
        assertTrue("Populations current value doesn't match", populationsValue.getCurrentValue().contains("-21"));
    }

    public void testAuditEventSubjectAssignmentPopulationUpdateFromNoneToSomeOne() throws Exception {
        {
            StudySubjectAssignment loaded = studySubjectAssignmentDao.getById(-11);
            Population population = populationDao.getById(-20);
            loaded.addPopulation(population);
            studySubjectAssignmentDao.save(loaded);
        }

        interruptSession();
        int eventId = getEventIdForObject(-11, StudySubjectAssignment.class.getName(), Operation.UPDATE.name(), "populations");
        assertNotNull("Audit event for UPDATE is not created ", eventId);

        DataAuditEventValue populationsValue = getAuditEventValueFor(eventId, "populations");

        // Testing populations value
        assertNotNull("No data audit event value for populations", populationsValue);
        assertEquals("Populations previous value doesn't match", null, populationsValue.getPreviousValue());
        assertEquals("Populations current value doesn't match", "-20", populationsValue.getCurrentValue());
    }

    public void testAuditEventSubjectAssignmentPopulationUpdateFromNoneToTwoValues() throws Exception {
        {
            StudySubjectAssignment loaded = studySubjectAssignmentDao.getById(-11);
            Population population1 = populationDao.getById(-20);
            Population population2 = populationDao.getById(-21);
            loaded.addPopulation(population1);
            loaded.addPopulation(population2);
            studySubjectAssignmentDao.save(loaded);
        }

        interruptSession();
        int eventId = getEventIdForObject(-11, StudySubjectAssignment.class.getName(), Operation.UPDATE.name(), "populations");
        assertNotNull("Audit event for UPDATE is not created ", eventId);

        DataAuditEventValue populationsValue = getAuditEventValueFor(eventId, "populations");

        // Testing populations value
        assertNotNull("No data audit event value for populations", populationsValue);
        assertEquals("Populations previous value doesn't match", null, populationsValue.getPreviousValue());
        assertTrue("Populations current value doesn't match", populationsValue.getCurrentValue().contains("-20"));
        assertTrue("Populations current value doesn't match", populationsValue.getCurrentValue().contains("-21"));
    }

    public void testAuditEventSubjectAssignmentPopulationUpdateFromSomeOneToNone() throws Exception {
        {
            StudySubjectAssignment loaded = studySubjectAssignmentDao.getById(-10);
            Population population = populationDao.getById(-21);
            loaded.getPopulations().remove(population);
            studySubjectAssignmentDao.save(loaded);
        }

        interruptSession();
        int eventId = getEventIdForObject(-10, StudySubjectAssignment.class.getName(), Operation.UPDATE.name(), "populations");
        assertNotNull("Audit event for UPDATE is not created ", eventId);

        DataAuditEventValue populationsValue = getAuditEventValueFor(eventId, "populations");

        // Testing populations value
        assertNotNull("No data audit event value for populations", populationsValue);
        assertEquals("Populations previous value doesn't match", "-21", populationsValue.getPreviousValue());
        assertEquals("Populations current value doesn't match", null, populationsValue.getCurrentValue());
    }

    public void testAuditEventForScheduledActivityLabelsUpdate() throws Exception {
        {
            ScheduledActivity loaded = scheduledActivityDao.getById(-10);
            loaded.getLabels().add("research");
            scheduledActivityDao.save(loaded);
        }

        interruptSession();
        int eventId = getEventIdForObject(-10, ScheduledActivity.class.getName(), Operation.UPDATE.name(), "labels");
        assertNotNull("Audit event for UPDATE is not created ", eventId);

        DataAuditEventValue value = getAuditEventValueFor(eventId, "labels");

        // Testing labels value
        assertNotNull("No data audit event value for Labels", value);
        assertEquals("Labels previous value doesn't match", "clean-only soc+test", value.getPreviousValue());
        assertEquals("Labels current value doesn't match", "clean-only research soc+test", value.getCurrentValue());
    }

    public void testAuditEventForScheduledActivityLabelsUpdateSomeToNone() throws Exception {
        {
            ScheduledActivity loaded = scheduledActivityDao.getById(-10);
            loaded.getLabels().remove("soc test");
            loaded.getLabels().remove("clean-only");
            scheduledActivityDao.save(loaded);
        }

        interruptSession();
        int eventId = getEventIdForObject(-10, ScheduledActivity.class.getName(), Operation.UPDATE.name(), "labels");
        assertNotNull("Audit event for UPDATE is not created ", eventId);

        DataAuditEventValue value = getAuditEventValueFor(eventId, "labels");

        // Testing labels value
        assertNotNull("No data audit event value for Labels", value);
        assertEquals("Labels previous value doesn't match", "clean-only soc+test", value.getPreviousValue());
        assertEquals("Labels current value doesn't match", null, value.getCurrentValue());
    }

    public void testAuditEventForScheduledActivityLabelsUpdateNoneToSome() throws Exception {
        {
            ScheduledActivity loaded = scheduledActivityDao.getById(-17);
            loaded.getLabels().add("old,male");
            loaded.getLabels().add("mri");
            scheduledActivityDao.save(loaded);
        }

        interruptSession();
        int eventId = getEventIdForObject(-17, ScheduledActivity.class.getName(), Operation.UPDATE.name(), "labels");
        assertNotNull("Audit event for UPDATE is not created ", eventId);

        DataAuditEventValue value = getAuditEventValueFor(eventId, "labels");

        // Testing labels value
        assertNotNull("No data audit event value for Labels", value);
        assertEquals("Labels previous value doesn't match", null, value.getPreviousValue());
        assertEquals("Labels current value doesn't match", "mri old%2Cmale", value.getCurrentValue());
    }

    public void testAuditEventForSubjectPropertiesUpdate() throws Exception {
        {
            Subject loaded = subjectDao.getById(-20);
            loaded.getProperties().remove(0);
            subjectDao.save(loaded);
        }

        interruptSession();
        int eventId = getEventIdForObject(-20, Subject.class.getName(), Operation.UPDATE.name(), "properties");
        assertNotNull("Audit event for UPDATE is not created ", eventId);
        DataAuditEventValue value = getAuditEventValueFor(eventId, "properties");

        // Testing properties value
        assertNotNull("No data audit event value for Properties", value);
        assertEquals("Properties previous value doesn't match",
                "Preferred%2BContact%2BAddress+world%2540bridge.us Hair%2BColor+blue", value.getPreviousValue());
        assertEquals("Properties current value doesn't match",
                "Hair%2BColor+blue", value.getCurrentValue());
    }

    public void testAuditEventForSubjectPropertiesUpdateFromSomeToNone() throws Exception {
        {

            Subject loaded = subjectDao.getById(-20);
            loaded.getProperties().remove(0);
            loaded.getProperties().remove(0);
            subjectDao.save(loaded);
        }

        interruptSession();
        int eventId = getEventIdForObject(-20, Subject.class.getName(), Operation.UPDATE.name(), "properties");
        assertNotNull("Audit event for UPDATE is not created ", eventId);
        DataAuditEventValue value = getAuditEventValueFor(eventId, "properties");

        // Testing properties value
        assertNotNull("No data audit event value for Properties", value);
        assertEquals("Properties previous value doesn't match",
                "Preferred%2BContact%2BAddress+world%2540bridge.us Hair%2BColor+blue", value.getPreviousValue());
        assertEquals("Properties current value doesn't match",
                null, value.getCurrentValue());
    }

    public void testAuditEventForSubjectPropertiesUpdateFromNoneToSome() throws Exception {
        {

            Subject loaded = subjectDao.getById(-21);
            loaded.getProperties().add(new SubjectProperty("Seat Preference", "window"));
            loaded.getProperties().add(new SubjectProperty("Meal Preference", "vegetarian"));
            subjectDao.save(loaded);
        }

        interruptSession();
        int eventId = getEventIdForObject(-21, Subject.class.getName(), Operation.UPDATE.name(), "properties");
        assertNotNull("Audit event for UPDATE is not created ", eventId);
        DataAuditEventValue value = getAuditEventValueFor(eventId, "properties");

        // Testing properties value
        assertNotNull("No data audit event value for Properties", value);
        assertEquals("Properties previous value doesn't match", null, value.getPreviousValue());
        assertEquals("Properties current value doesn't match",
                "Seat%2BPreference+window Meal%2BPreference+vegetarian", value.getCurrentValue());
    }

}

