package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Simulates a round trip XML export and import.
 *
 * @author Rhett Sutphin
 */
public class XmlExportImportIntegratedTest extends DaoTestCase {
    private StudyService studyService
        = (StudyService) getApplicationContext().getBean("studyService");
    private AmendmentService amendmentService
        = (AmendmentService) getApplicationContext().getBean("amendmentService");
    private DeltaService deltaService
        = (DeltaService) getApplicationContext().getBean("deltaService");
    private ImportTemplateService importTemplateService
        = (ImportTemplateService) getApplicationContext().getBean("importTemplateService");

    private StudyDao studyDao
        = (StudyDao) getApplicationContext().getBean("studyDao");
    private ActivityDao activityDao
        = (ActivityDao) getApplicationContext().getBean("activityDao");

    private StudyXmlSerializer serializer
        = (StudyXmlSerializer) getApplicationContext().getBean("studyXmlSerializer");

    private int studyId;

    public void setUp() throws Exception {
        super.setUp();
        Study created = TemplateSkeletonCreator.BASIC.create("Exportable");
        Add add = (Add) created.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0);
        Period period = createPeriod(1, 7, 4);
        period.addPlannedActivity(createPlannedActivity(activityDao.getById(-1), 2));
        ((Epoch) add.getChild()).getStudySegments().get(0).addPeriod(period);
        studyService.save(created);

        interruptSession();

        studyId = created.getId();
    }

    private InputStream export() {
        String xml = serializer.createDocumentString(reload());
        ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
        interruptSession();
        return input;
    }

    private Study reimport() {
        importTemplateService.readAndSaveTemplate(reload(), export());
        interruptSession();
        return reload();
    }

    private Study reload() {
        Study study = studyDao.getById(studyId);
        assertNotNull("Test setup failure: could not reload using expected ID", study);
        return study;
    }

    public void testExportImportWithDevAmendmentOnly() throws Exception {
        Study actual = reimport();
        assertNotNull("Dev amendment missing", actual.getDevelopmentAmendment());
        assertNull("Should be no released amendments", actual.getAmendment());

        Add add = (Add) actual.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0);
        assertNotNull("Add not found", add);
        PlanTreeNode child = deltaService.findChangeChild(add);
        assertTrue(child instanceof Epoch);
        Epoch actualEpoch = (Epoch) child;
        assertEquals("Wrong epoch", "Treatment", actualEpoch.getName());
        // assuming everything else
    }

    /* TODO: This test fails -- make it work
    public void testExportImportWithSingleReleasedAmendment() throws Exception {
        amendmentService.amend(reload());

        Study actual = reimport();
        assertNull("Should have no dev amendment", actual.getDevelopmentAmendment());
        assertNotNull("Should have a released amendment", actual.getAmendment());

        Add add = (Add) actual.getAmendment().getDeltas().get(0).getChanges().get(0);
        assertNotNull("Add not found", add);
        PlanTreeNode child = deltaService.findChangeChild(add);
        assertTrue(child instanceof Epoch);
        Epoch actualEpoch = (Epoch) child;
        assertEquals("Wrong epoch", "Treatment", actualEpoch.getName());
    }
    */
}
