package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Element;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;

public class PlannedCalendarDeltaXmlSerializerTest extends StudyCalendarXmlTestCase {
    private AbstractDeltaXmlSerializer serializer;
    private Delta plannedCalendarDelta;
    private DeltaDao deltaDao;
    private Element element;
    private Study study;
    private PlannedCalendar calendar;
    private TemplateService templateService;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        deltaDao = registerDaoMockFor(DeltaDao.class);
        templateService = registerMockFor(TemplateService.class);

        calendar = setGridId("grid1", new PlannedCalendar());
        study = createNamedInstance("Study A", Study.class);
        study.setPlannedCalendar(calendar);

        serializer = new PlannedCalendarDeltaXmlSerializer(study);
        serializer.setDeltaDao(deltaDao);
        serializer.setTemplateService(templateService);


        plannedCalendarDelta = Delta.createDeltaFor(calendar, new Add());
        plannedCalendarDelta.setGridId("grid0");
    }

    public void testCreateElement() {
        Element element = serializer.createElement(plannedCalendarDelta);

        assertEquals("Wrong element name", "planned-calendar-delta", element.getName());
        assertEquals("Wrong node id", "grid1", element.attributeValue("node-id"));
        assertEquals("Wrong id", "grid0", element.attributeValue("id"));
    }

    public void testReadElementWithExistingDelta() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(deltaDao.getByGridId("grid0")).andReturn(plannedCalendarDelta);
        replayMocks();

        Delta actual = serializer.readElement(element);
        verifyMocks();

        assertSame("Deltas should be the same", plannedCalendarDelta, actual);
    }

    public void testReadElementWithNewAmendment() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(deltaDao.getByGridId("grid0")).andReturn(null);
        expect(element.attributeValue("node-id")).andReturn("grid1");
        expect(templateService.findEquivalentChild(EasyMock.eq(study), eqGridId(calendar))).andReturn((PlanTreeNode)calendar);
        replayMocks();

        Delta actual = serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong delta grid id", "grid0", actual.getGridId());
        assertEquals("Wrong delta grid id", "grid1", actual.getNode().getGridId());

    }
}
