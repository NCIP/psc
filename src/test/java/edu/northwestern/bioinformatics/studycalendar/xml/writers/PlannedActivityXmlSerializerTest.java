package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

public class PlannedActivityXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PlannedActivityXmlSerializer serializer;
    private PlannedActivityDao plannedActivityDao;
    private Element element;
    private PlannedActivity plannedActivity;
    private ActivityXmlSerializer activitySerializer;
    private PlannedActivityLabelXmlSerializer plannedActivityLabelXmlSerializer = new PlannedActivityLabelXmlSerializer();
    private Element eActivity;
    private List<PlannedActivity> plannedActivities;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        activitySerializer = registerMockFor(ActivityXmlSerializer.class);

        Population population = Fixtures.createPopulation("MP", "My Populaiton");
        plannedActivity = setGridId("grid0", Fixtures.createPlannedActivity("Bone Scan", 2, "scan details", "no mice"));
        plannedActivity.setPopulation(population);

        Study study = createNamedInstance("Study A", Study.class);
        study.addPopulation(population);

        eActivity = DocumentHelper.createElement("activity");

        serializer = new PlannedActivityXmlSerializer();
        serializer.setPlannedActivityDao(plannedActivityDao);
        serializer.setStudy(study);
        serializer.setActivityXmlSerializer(activitySerializer);
        serializer.setPlannedActivityLabelXmlSerializer(plannedActivityLabelXmlSerializer);
        plannedActivities = new ArrayList<PlannedActivity>();
        plannedActivity.addPlannedActivityLabel(Fixtures.createPlannedActivityLabel("testlabel"));
    }

    public void testCreateElementPlannedActivity() {
        expect(activitySerializer.createElement(plannedActivity.getActivity())).andReturn(eActivity);
        replayMocks();

        Element actual = serializer.createElement(plannedActivity);
        verifyMocks();
        assertEquals("Wrong label name","testlabel",actual.element("label").attributeValue("name"));
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
        expect(element.element("activity")).andReturn(eActivity);
        expect(activitySerializer.readElement(eActivity)).andReturn(new Activity());

        replayMocks();

        PlannedActivity actual = (PlannedActivity) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong day", 2, (int) actual.getDay());
        assertEquals("Wrong details", "scan details", actual.getDetails());
        assertEquals("Wrong condition", "no mice", actual.getCondition());
        assertEquals("Wrong population", "MP", actual.getPopulation().getAbbreviation());
        assertNotNull("Activity should exist", actual.getActivity());
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

    public void testValidateElement() throws Exception {
        activitySerializer = new ActivityXmlSerializer();
        serializer.setActivityXmlSerializer(activitySerializer);
        PlannedActivity plannedActivity = createPlannedActivity();

        Element actual = serializer.createElement(plannedActivity);

        assertTrue(StringUtils.isBlank(serializer.validateElement(plannedActivity, actual).toString()));
        plannedActivity.setGridId("wrong grid id");
        assertFalse(StringUtils.isBlank(serializer.validateElement(plannedActivity, actual).toString()));

        plannedActivity = createPlannedActivity();
        assertTrue(StringUtils.isBlank(serializer.validateElement(plannedActivity, actual).toString()));
        plannedActivity.setDay(null);
        assertFalse(StringUtils.isBlank(serializer.validateElement(plannedActivity, actual).toString()));

        plannedActivity = createPlannedActivity();
        assertTrue(StringUtils.isBlank(serializer.validateElement(plannedActivity, actual).toString()));
        plannedActivity.setCondition("wrong condition");
        assertFalse(StringUtils.isBlank(serializer.validateElement(plannedActivity, actual).toString()));

        plannedActivity = createPlannedActivity();
        assertTrue(StringUtils.isBlank(serializer.validateElement(plannedActivity, actual).toString()));
        plannedActivity.setPopulation(null);
        assertFalse(StringUtils.isBlank(serializer.validateElement(plannedActivity, actual).toString()));

        Population population = Fixtures.createPopulation("wrong MP", "Populaiton");

        plannedActivity = createPlannedActivity();
        assertTrue(StringUtils.isBlank(serializer.validateElement(plannedActivity, actual).toString()));
        plannedActivity.setPopulation(population);
        assertFalse(StringUtils.isBlank(serializer.validateElement(plannedActivity, actual).toString()));

        plannedActivity = createPlannedActivity();
        population = Fixtures.createPopulation("MP", "wrong Populaiton");
        plannedActivity.setPopulation(population);
        actual = serializer.createElement(plannedActivity);
        assertTrue(StringUtils.isBlank(serializer.validateElement(plannedActivity, actual).toString()));
       plannedActivity.setPopulation(null);
        assertFalse(StringUtils.isBlank(serializer.validateElement(plannedActivity, actual).toString()));


    }

    public void testGetPlannedActivityWithMatchingAttributes() throws Exception {
        activitySerializer = new ActivityXmlSerializer();
        serializer.setActivityXmlSerializer(activitySerializer);
        PlannedActivity plannedActivity = createPlannedActivity();

        Element actual = serializer.createElement(plannedActivity);


        assertNotNull(serializer.getPlannedActivityWithMatchingGridId(plannedActivities, actual));


    }

    private PlannedActivity createPlannedActivity() {

        Population population = Fixtures.createPopulation("MP", "My Populaiton");
        PlannedActivity plannedActivity = setGridId("grid0", Fixtures.createPlannedActivity("Bone Scan", 2, "scan details", "no mice"));
        plannedActivity.setPopulation(population);
        plannedActivities.clear();
        plannedActivities.add(plannedActivity);
        return plannedActivity;
    }
}
