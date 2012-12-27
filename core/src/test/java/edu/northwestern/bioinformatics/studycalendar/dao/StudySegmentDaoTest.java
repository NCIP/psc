/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Nov 25, 2007
 * Time: 2:38:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class StudySegmentDaoTest extends DaoTestCase {
    private StudySegmentDao dao = (StudySegmentDao) getApplicationContext().getBean("studySegmentDao");

    public void testGetByIdAndTheHibernateMapping() {
        StudySegment studySegment = dao.getById(-2);
        assertNotNull("studySegment not found", studySegment);
        assertEquals("name wrong", "Treatment", studySegment.getName());
        assertEquals("wrong number of periods", 2, studySegment.getPeriods().size());
        Map<String, Period> periodsByName = getPeriodsByName(studySegment);
        assertTrue("Pre-screening period not present", periodsByName.containsKey("Pre-screening"));
        assertTrue("Treatment period not present", periodsByName.containsKey("Treatment"));

        Period treatment = periodsByName.get("Treatment");
        assertEquals("Wrong studySegment for treatment period", studySegment, treatment.getStudySegment());
        assertEquals("Wrong start day for treatment", new Integer(8), treatment.getStartDay());
        assertEquals("Wrong duration for treatment", new Duration(6, Duration.Unit.week), treatment.getDuration());
        assertEquals("Wrong repetitions for treatment", 3, treatment.getRepetitions());
    }

    private Map<String, Period> getPeriodsByName(StudySegment studySegment) {
        Map<String, Period> periodsByName = new HashMap<String, Period>();
        for(Period period : studySegment.getPeriods()) {
            periodsByName.put(period.getName(), period);
        }
        return periodsByName;
    }

    public void testUpdateStudySegment() throws Exception {
        {
            StudySegment studySegment = dao.getById(-2);
            Period newPeriod = new Period();
            newPeriod.setName("Checkup");
            newPeriod.setStartDay(1);
            newPeriod.setDuration(new Duration(7, Duration.Unit.day));
            newPeriod.setRepetitions(1);
            studySegment.addPeriod(newPeriod);

            dao.save(studySegment);
        }

        interruptSession();

        {
            StudySegment loaded = dao.getById(-2);
            assertEquals("Wrong number of periods", 3, loaded.getPeriods().size());
            Map<String, Period> periodsByName = getPeriodsByName(loaded);
            assertTrue("Checkup period not found", periodsByName.containsKey("Checkup"));
            Period checkup = periodsByName.get("Checkup");
            assertEquals("Wrong duration for checkup period", new Duration(7, Duration.Unit.day), checkup.getDuration());
            assertEquals("Wrong repetitions for checkup period", 1, checkup.getRepetitions());
        }
    }

    public void testSaveStudySegmentWithoutEpoch() throws Exception {
        Integer id;
        {
            StudySegment studySegment = new StudySegment();
            studySegment.setName("Hula");

            dao.save(studySegment);
            assertNotNull("Not saved", studySegment.getId());
            id = studySegment.getId();
        }

        interruptSession();

        {
            StudySegment loaded = dao.getById(id);
            assertEquals("Wrong studySegment", "Hula", loaded.getName());
        }
    }

    public void testDetachStudySegmentFromEpoch() throws Exception {
        {
            StudySegment studySegment = dao.getById(-2);
            studySegment.getParent().removeChild(studySegment);
            dao.save(studySegment);
        }

        interruptSession();

        StudySegment reloaded = dao.getById(-2);
        assertNotNull("Could not reload segment", reloaded);
        assertNull(reloaded.getEpoch());
    }

    public void testLoadCycleLength() {
        StudySegment studySegment = dao.getById(-2);
        assertEquals("The length doesn't match", new Integer(14), studySegment.getCycleLength());
    }

    public void testDeleteJustStudySegmentAsPlainOrphan() throws Exception {
        StudySegment ss = dao.getById(-10);
        assertNotNull(ss);
        assertTrue("StudySegment is attached ", ss.isDetached());
        assertNull("StudySegment has a parent ", ss.getParent());
        dao.deleteOrphans();
        assertNull(dao.getById(-10));
    }

    public void testDeleteStudySegmentWithParent() throws Exception {
        StudySegment ss = dao.getById(-101);
        assertNotNull(ss);
        assertNotNull("StudySegment does not have a parent ", ss.getParent());
        dao.deleteOrphans();
        assertNotNull(dao.getById(-101));
    }

    public void testToDeleteStudySegmentWithAddOnly() throws Exception {
        StudySegment ss = dao.getById(-18);
        assertTrue("StudySegment is attached ", ss.isDetached());
        assertNull("StudySegment has a parent ", ss.getParent());
        dao.deleteOrphans();
        assertNotNull(dao.getById(-18));
    }

    public void testToDeleteStudySegmentWithRemoveOnly() throws Exception {
        StudySegment ss = dao.getById(-100);
        assertTrue("StudySegment is attached ", ss.isDetached());
        assertNull("StudySegment has a parent ", ss.getParent());
        dao.deleteOrphans();
        assertNotNull(dao.getById(-100));
    }
}

