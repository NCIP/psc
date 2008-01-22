package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import org.dom4j.Document;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import static java.text.MessageFormat.format;

// TODO: Delete me when PlanTreeNodeXmlSerializer refactoring is done
public abstract class PlanTreeNodeXmlSerializerTest extends StudyCalendarXmlTestCase {

    private PlanTreeNodeXmlSerializer serializer;
    private EpochXmlSerializer epochSerializer;
    private Element element;
    private Epoch epoch;
    private EpochDao epochDao;
    private StudySegment segment;
    private PeriodDao periodDao;
    private StudySegmentDao studySegmentDao;
    private PlannedActivityDao plannedActivityDao;
    private Period period;
    private PlannedActivity plannedActivity;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        epochDao = registerDaoMockFor(EpochDao.class);
        periodDao = registerDaoMockFor(PeriodDao.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);

//        serializer = new PlanTreeNodeXmlSerializer();
//        serializer.setEpochDao(epochDao);
//        serializer.setPeriodDao(periodDao);
//        serializer.setStudySegmentDao(studySegmentDao);
//        serializer.setPlannedActivityDao(plannedActivityDao);
        epochSerializer = new EpochXmlSerializer();
        epochSerializer.setEpochDao(epochDao);
        serializer = null;

        epoch = setGridId("grid0", createNamedInstance("Epoch A", Epoch.class));
        segment = setGridId("grid1", createNamedInstance("Segment A", StudySegment.class));
        period = setGridId("grid2", createNamedInstance("Period A", Period.class));

        Population population = new Population();
        population.setAbbreviation("PP");
        plannedActivity = setGridId("grid3", Fixtures.createPlannedActivity("bone scan", 2, "scan details", "no mice"));
        plannedActivity.setPopulation(population);



    }

    public void testCreateElementEpoch() {
        Element actual = epochSerializer.createElement(epoch);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
        assertEquals("Wrong epoch name", "Epoch A", actual.attribute("name").getValue());
    }

    public void testReadElementEpoch() {
        expect(element.getName()).andReturn("epoch");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(epochDao.getByGridId("grid0")).andReturn(null);
        expect(element.attributeValue("name")).andReturn("Epoch A");
        replayMocks();

        Epoch actual = (Epoch) epochSerializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong epoch name", "Epoch A", actual.getName());
    }

    public void testReadElementExistsEpoch() {
        expect(element.getName()).andReturn("epoch");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(epochDao.getByGridId("grid0")).andReturn(epoch);
        replayMocks();

        Epoch actual = (Epoch) epochSerializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Epoch", epoch, actual);
    }

    public void testCreateDocumentEpoch() throws Exception {
        Document actual = epochSerializer.createDocument(epoch);

        assertEquals("Element should be an epoch", "epoch", actual.getRootElement().getName());
        assertEquals("Wrong epoch grid id", "grid0", actual.getRootElement().attributeValue("id"));
        assertEquals("Wrong epoch name", "Epoch A", actual.getRootElement().attributeValue("name"));
    }

    public void testCreateDocumentStringEpoch() throws Exception {
        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append(format("<epoch id=\"{0}\" name=\"{1}\"", epoch.getGridId(), epoch.getName()))
                .append(format("       {0}=\"{1}\""     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
                .append(format("       {0}=\"{1} {2}\""     , SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, SCHEMA_LOCATION))
                .append(format("       {0}=\"{1}\"/>"    , XML_SCHEMA_ATTRIBUTE, XSI_NS));

        String actual = epochSerializer.createDocumentString(epoch);
        assertXMLEqual(expected.toString(), actual);
    }

    public void testCreateElementStudySegment() {
        Element actual = serializer.createElement(segment);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong grid id", "grid1", actual.attribute("id").getValue());
        assertEquals("Wrong segment name", "Segment A", actual.attribute("name").getValue());
    }

    public void testReadElementStudySegment() {
        expect(element.attributeValue("id")).andReturn("grid1");
        expect(element.getName()).andReturn("study-segment").times(2);
        expect(studySegmentDao.getByGridId("grid1")).andReturn(null);
        expect(element.attributeValue("name")).andReturn("Segment A");
        replayMocks();

        StudySegment actual = (StudySegment) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid1", actual.getGridId());
        assertEquals("Wrong segment name", "Segment A", actual.getName());
    }

    public void testReadElementExistsStudySegment() {
        expect(element.attributeValue("id")).andReturn("grid1");
        expect(element.getName()).andReturn("study-segment");
        expect(studySegmentDao.getByGridId("grid1")).andReturn(segment);
        replayMocks();

        StudySegment actual = (StudySegment) serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Epoch", segment, actual);
    }

    public void testCreateElementPeriod() {
        Element actual = serializer.createElement(period);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong grid id", "grid2", actual.attribute("id").getValue());
        assertEquals("Wrong period name", "Period A", actual.attribute("name").getValue());
    }

    public void testReadElementStudyPeriod() {
        expect(element.attributeValue("id")).andReturn("grid2");
        expect(element.getName()).andReturn("period").times(2);
        expect(periodDao.getByGridId("grid2")).andReturn(null);
        expect(element.attributeValue("name")).andReturn("Period A");
        replayMocks();

        Period actual = (Period) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid2", actual.getGridId());
        assertEquals("Wrong period name", "Period A", actual.getName());
    }

    public void testReadElementExistsPeriod() {
        expect(element.attributeValue("id")).andReturn("grid2");
        expect(element.getName()).andReturn("period");
        expect(periodDao.getByGridId("grid2")).andReturn(period);
        replayMocks();

        Period actual = (Period) serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Period", period, actual);
    }

    public void testCreateElementPlannedActivity() {
        Element actual = serializer.createElement(plannedActivity);

        assertEquals("Wrong attribute size", 5, actual.attributeCount());
        assertEquals("Wrong grid id", "grid3", actual.attribute("id").getValue());
        assertEquals("Wrong day", "2", actual.attributeValue("day"));
        assertEquals("Wrong details", "scan details", actual.attributeValue("details"));
        assertEquals("Wrong condition", "no mice", actual.attributeValue("condition"));
        assertEquals("Wrong population", "PP", actual.attributeValue("population"));
    }

    public void testReadElementStudyPlannedActivity() {
        expect(element.getName()).andReturn("planned-activity").times(2);
        expect(element.attributeValue("id")).andReturn("grid3");
        expect(plannedActivityDao.getByGridId("grid3")).andReturn(null);
        expect(element.attributeValue("day")).andReturn("2");
        expect(element.attributeValue("details")).andReturn("scan details");
        expect(element.attributeValue("condition")).andReturn("no mice");
        replayMocks();

        PlannedActivity actual = (PlannedActivity) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid3", actual.getGridId());
        assertEquals("Wrong day", 2, (int) actual.getDay());
        assertEquals("Wrong details", "scan details", actual.getDetails());
        assertEquals("Wrong condition", "no mice", actual.getCondition());
    }

    public void testReadElementExistsPlannedActivity() {
        expect(element.attributeValue("id")).andReturn("grid3");
        expect(element.getName()).andReturn("planned-activity");
        expect(plannedActivityDao.getByGridId("grid3")).andReturn(plannedActivity);
        replayMocks();

        PlannedActivity actual = (PlannedActivity) serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Planned Activity", plannedActivity, actual);
    }
}
