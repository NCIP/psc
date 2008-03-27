package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer.*;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import static org.easymock.EasyMock.expect;

import static java.text.MessageFormat.format;
import java.util.Calendar;
import java.util.Collections;

public class AmendmentXmlSerializerTest extends StudyCalendarXmlTestCase {
    private AmendmentDao amendmentDao;
    private AmendmentXmlSerializer serializer;
    private Amendment amendment1;
    private Element element;
    private Amendment amendment0;
    private Element eAmendment;
    private AbstractDeltaXmlSerializer deltaSerializer;
    private Element eDelta;
    private PlannedCalendarDelta delta;
    private DeltaXmlSerializerFactory deltaSerializerFactory;
    private Study study;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        deltaSerializer = registerMockFor(AbstractDeltaXmlSerializer.class);
        deltaSerializerFactory = registerMockFor(DeltaXmlSerializerFactory.class);

        amendment0 = setGridId("grid0", new Amendment());
        amendment0.setName("Amendment 0");
        amendment0.setDate(createDate(2008, Calendar.JANUARY, 1));

        delta = setGridId("grid1", new PlannedCalendarDelta());
        delta.setNode(setGridId("grid2", new PlannedCalendar()));

        amendment1 =  new Amendment();
        amendment1.setMandatory(true);
        amendment1.setName("Amendment 1");
        amendment1.setPreviousAmendment(amendment0);
        amendment1.setDate(createDate(2008, Calendar.JANUARY, 2));
        amendment1.addDelta(delta);

        QName qDelta = DocumentHelper.createQName("planned-calendar-delta", AbstractStudyCalendarXmlSerializer.DEFAULT_NAMESPACE);
        eDelta = DocumentHelper.createElement(qDelta);
        eDelta.addAttribute("id", delta.getGridId());
        eDelta.addAttribute("node-id", "grid2");

        QName qAmendment = DocumentHelper.createQName("amendment", AbstractStudyCalendarXmlSerializer.DEFAULT_NAMESPACE);
        eAmendment = DocumentHelper.createElement(qAmendment);
        eAmendment.addAttribute("mandatory", Boolean.valueOf(amendment1.isMandatory()).toString());
        eAmendment.addAttribute("name", amendment1.getName());

        study = createNamedInstance("Study A", Study.class);
        study.setAmendment(amendment0);

        serializer = new AmendmentXmlSerializer() {
            public DeltaXmlSerializerFactory getDeltaXmlSerializerFactory() {
                return deltaSerializerFactory;
            }

        };
        serializer.setStudy(study);
        serializer.setAmendmentDao(amendmentDao);
    }

    public void testCreateElement() {
        expect(deltaSerializerFactory.createXmlSerializer(delta)).andReturn(deltaSerializer);
        expect(deltaSerializer.createElement(delta)).andReturn(eDelta);
        replayMocks();

        Element actual = serializer.createElement(amendment1);
        verifyMocks();

        assertEquals("Wrong attribute size", 4, actual.attributeCount());
        assertEquals("Wrong name", "Amendment 1", actual.attributeValue("name"));
        assertEquals("Wrong date", "2008-01-02", actual.attributeValue("date"));
        assertEquals("Wrong mandatory value", "true", actual.attributeValue("mandatory"));
        assertEquals("Wrong previous amdendment id", "2008-01-01~Amendment 0", actual.attributeValue("previous-amendment-key"));
        assertEquals("Should be more than one element", 1, actual.elements("planned-calendar-delta").size());
    }

    public void testReadElementWithExistingAmendment() {
        expect(element.attributeValue("name")).andReturn("Amendment 1");
        expect(element.attributeValue("date")).andReturn("2007-01-02");
        expect(amendmentDao.getByNaturalKey("2007-01-02~Amendment 1", study)).andReturn(amendment1);
        replayMocks();
        
        Amendment actual = serializer.readElement(element);
        verifyMocks();

        assertSame("Amendments should be the same", amendment1, actual);
    }

    public void testReadElementWithNewAmendment() {
        expect(element.attributeValue("name")).andReturn("Amendment 1");
        expect(element.attributeValue("date")).andReturn("2008-01-02");
        expect(element.attributeValue("mandatory")).andReturn("true");
        expect(amendmentDao.getByNaturalKey("2008-01-02~Amendment 1", study)).andReturn(null);
        expect(element.attributeValue("previous-amendment-key")).andReturn("2008-01-01~Amendment 0");

        expect(element.elements()).andReturn(Collections.singletonList(eDelta));
        expect(deltaSerializerFactory.createXmlSerializer(eDelta)).andReturn(deltaSerializer);
        expect(deltaSerializer.readElement(eDelta)).andReturn(delta);
        replayMocks();

        Amendment actual = serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong name", "Amendment 1", actual.getName());
        assertSameDay("Wrong date", createDate(2008, Calendar.JANUARY, 2), actual.getDate());
        assertTrue("Should be mandatory", actual.isMandatory());
        assertSame("Wrong previous amendment", amendment0, actual.getPreviousAmendment());
    }

    public void testCreateDocumentString() throws Exception {
        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append(format("<amendment name=\"{0}\" date=\"2008-01-02\"", amendment1.getName()));
        expected.append(format("           mandatory=\"true\" previous-amendment-key=\"{0}\"", amendment1.getPreviousAmendment().getNaturalKey()));
        expected.append(format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
        expected.append(format("       {0}:{1}=\"{2} {3}\"", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));
        expected.append(format("       {0}:{1}=\"{2}\">", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));
        expected.append("<planned-calendar-delta id=\"grid1\" node-id=\"grid2\"/>");
        expected.append("</amendment>");

        expect(deltaSerializerFactory.createXmlSerializer(delta)).andReturn(deltaSerializer);
        expect(deltaSerializer.createElement(delta)).andReturn(eDelta);
        replayMocks();

        String actual = serializer.createDocumentString(amendment1);
        verifyMocks();

        assertXMLEqual(expected.toString(), actual);
    }
}
