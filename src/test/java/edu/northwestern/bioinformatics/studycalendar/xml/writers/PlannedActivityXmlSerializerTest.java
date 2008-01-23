package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

public class PlannedActivityXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PlannedActivityXmlSerializer serializer;
    private PlannedActivityDao plannedActivityDao;
    private Element element;
    private PlannedActivity plannedActivity;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);

        Population population = Fixtures.createPopulation("MP", "My Populaiton");
        plannedActivity = setGridId("grid0", Fixtures.createPlannedActivity("Bone Scan", 2, "scan details", "no mice"));
        plannedActivity.setPopulation(population);

        Study study = createNamedInstance("Study A", Study.class);
        study.addPopulation(population);
        serializer = new PlannedActivityXmlSerializer(study);
        serializer.setPlannedActivityDao(plannedActivityDao);


    }

    public void testCreateElementPlannedActivity() {
        Element actual = serializer.createElement(plannedActivity);

        assertEquals("Wrong attribute size", 5, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
        assertEquals("Wrong day", "2", actual.attributeValue("day"));
        assertEquals("Wrong details", "scan details", actual.attributeValue("details"));
        assertEquals("Wrong condition", "no mice", actual.attributeValue("condition"));
        assertEquals("Wrong population", "MP", actual.attributeValue("population"));
    }

    public void testReadElementStudyPlannedActivity() {
        expect(element.getName()).andReturn("planned-activity");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(plannedActivityDao.getByGridId("grid0")).andReturn(null);
        expect(element.attributeValue("day")).andReturn("2");
        expect(element.attributeValue("details")).andReturn("scan details");
        expect(element.attributeValue("condition")).andReturn("no mice");
        expect(element.attributeValue("population")).andReturn("MP");

        replayMocks();

        PlannedActivity actual = (PlannedActivity) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong day", 2, (int) actual.getDay());
        assertEquals("Wrong details", "scan details", actual.getDetails());
        assertEquals("Wrong condition", "no mice", actual.getCondition());
        assertEquals("Wrong population", "MP", actual.getPopulation().getAbbreviation());
    }

    public void testReadElementExistsPlannedActivity() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(element.getName()).andReturn("planned-activity");
        expect(plannedActivityDao.getByGridId("grid0")).andReturn(plannedActivity);
        replayMocks();

        PlannedActivity actual = (PlannedActivity) serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Planned Activity", plannedActivity, actual);
    }
}
