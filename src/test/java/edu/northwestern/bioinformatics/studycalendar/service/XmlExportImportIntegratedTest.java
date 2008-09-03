package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;

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
        return export(null);
    }

    private InputStream export(Study study) {
        String xml = serializer.createDocumentString(study == null ? reload() : study);
        ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
        interruptSession();
        return input;
    }

    private Study reimport() {
        return doImport(export());
    }

    private Study doImport(InputStream export) {
        importTemplateService.readAndSaveTemplate(reload(), export);
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
    
    public void testExportImportWithReleasedAmendmentAndNewReleasedAmendment() throws Exception {
        amendmentService.amend(reload());

        Study expectedExport = reload().transientClone();
        Epoch e1 = expectedExport.getPlannedCalendar().getEpochs().get(1);
        assertEquals("Test setup failure -- expected 1 segment in epoch 1 to start", 1, e1.getStudySegments().size());
        Amendment dev = createAmendment("A0", DateUtils.createDate(2008, Calendar.JANUARY, 3));
        Add newSegment = Add.create(Fixtures.createNamedInstance("New Segment", StudySegment.class));
        dev.addDelta(Delta.createDeltaFor(e1, newSegment));
        expectedExport.setDevelopmentAmendment(dev);
        Fixtures.amend(expectedExport);

        InputStream xml = export(expectedExport);
        Study actual = doImport(xml);

        assertNull("Should have no dev amendment", actual.getDevelopmentAmendment());
        assertNotNull("Should have a released amendment", actual.getAmendment());
        assertEquals("Released amendment should be A0", "A0", actual.getAmendment().getName());
        assertNotNull("Should have two released amendments, actually", actual.getAmendment().getPreviousAmendment());
        assertEquals("Prev amendment should be original", Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME,
            actual.getAmendment().getPreviousAmendment().getName());

        Epoch actualE1 = actual.getPlannedCalendar().getEpochs().get(1);
        assertEquals("Segment not added to live plan tree", 2, actualE1.getStudySegments().size());
        assertEquals("Wrong segment added to live plan tree", "New Segment", actualE1.getStudySegments().get(1).getName());
    }
}
