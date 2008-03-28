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
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import java.util.Collections;

public class PlannedCalendarDeltaXmlSerializerTest extends StudyCalendarXmlTestCase {
    private AbstractDeltaXmlSerializer serializer;
    private Delta plannedCalendarDelta;
    private DeltaDao deltaDao;
    private Element element;
    private Study study;
    private PlannedCalendar calendar;
    private TemplateService templateService;
    private ChangeXmlSerializerFactory changeSerializerFactory;
    private Add add;
    private AbstractChangeXmlSerializer changeSerializer;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        deltaDao = registerDaoMockFor(DeltaDao.class);
        templateService = registerMockFor(TemplateService.class);
        changeSerializerFactory = registerMockFor(ChangeXmlSerializerFactory.class);
        changeSerializer = registerMockFor(AbstractChangeXmlSerializer.class);

        calendar = setGridId("grid1", new PlannedCalendar());
        study = createNamedInstance("Study A", Study.class);
        study.setPlannedCalendar(calendar);

        serializer = new PlannedCalendarDeltaXmlSerializer(){
            public ChangeXmlSerializerFactory getChangeXmlSerializerFactory() {
                return changeSerializerFactory;
            }
        };
        serializer.setDeltaDao(deltaDao);
        serializer.setTemplateService(templateService);
        serializer.setStudy(study);


        add = new Add();
        plannedCalendarDelta = Delta.createDeltaFor(calendar, add);
        plannedCalendarDelta.setGridId("grid0");
    }

    public void testCreateElement() {
        expect(changeSerializerFactory.createXmlSerializer(add, calendar)).andReturn(changeSerializer);
        expect(changeSerializer.createElement(add)).andReturn(DocumentHelper.createElement("add"));
        replayMocks();

        Element element = serializer.createElement(plannedCalendarDelta);
        verifyMocks();

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
        expect(element.elements()).andReturn(Collections.emptyList());
        replayMocks();

        Delta actual = serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong delta grid id", "grid0", actual.getGridId());
        assertEquals("Wrong delta grid id", "grid1", actual.getNode().getGridId());

    }
}
