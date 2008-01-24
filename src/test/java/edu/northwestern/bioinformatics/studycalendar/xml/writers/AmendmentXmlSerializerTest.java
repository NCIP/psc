package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import static org.easymock.EasyMock.expect;

import static java.text.MessageFormat.format;
import java.util.Calendar;

public class AmendmentXmlSerializerTest extends StudyCalendarXmlTestCase {
    private AmendmentDao amendmentDao;
    private AmendmentXmlSerializer serializer;
    private Amendment amendment1;
    private Element element;
    private Amendment amendment0;
    private Element eAmendment;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);

        amendment0 = setGridId("grid0", new Amendment());
        amendment0.setName("Amendment 0");
        amendment0.setDate(createDate(2008, Calendar.JANUARY, 1));

        amendment1 =  new Amendment();
        amendment1.setMandatory(true);
        amendment1.setName("Amendment 1");
        amendment1.setPreviousAmendment(amendment0);
        amendment1.setDate(createDate(2008, Calendar.JANUARY, 2));

        QName qAmendment = DocumentHelper.createQName("amendment", AbstractStudyCalendarXmlSerializer.DEFAULT_NAMESPACE);
        eAmendment = DocumentHelper.createElement(qAmendment);
        eAmendment.addAttribute("mandatory", Boolean.valueOf(amendment1.isMandatory()).toString());
        eAmendment.addAttribute("name", amendment1.getName());

        Study study = createNamedInstance("Study A", Study.class);
        study.setAmendment(amendment0);
        serializer = new AmendmentXmlSerializer(study);
        serializer.setAmendmentDao(amendmentDao);
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(amendment1);

        assertEquals("Wrong attribute size", 4, actual.attributeCount());
        assertEquals("Wrong name", "Amendment 1", actual.attributeValue("name"));
        assertEquals("Wrong date", "2008-01-02", actual.attributeValue("date"));
        assertEquals("Wrong mandatory value", "true", actual.attributeValue("mandatory"));
        assertEquals("Wrong previous amdendment id", "2008-01-01~Amendment 0", actual.attributeValue("previous-amendment-key"));
    }

    public void testReadElementWithExistingAmendment() {
        expect(element.attributeValue("name")).andReturn("Amendment 1");
        expect(element.attributeValue("date")).andReturn("2007-01-02");
        expect(amendmentDao.getByNaturalKey("2007-01-02~Amendment 1")).andReturn(amendment1);
        replayMocks();
        
        Amendment actual = serializer.readElement(element);
        verifyMocks();

        assertSame("Amendments should be the same", amendment1, actual);
    }

    public void testReadElementWithNewAmendment() {
        expect(element.attributeValue("name")).andReturn("Amendment 1");
        expect(element.attributeValue("date")).andReturn("2008-01-02");
        expect(element.attributeValue("mandatory")).andReturn("true");
        expect(amendmentDao.getByNaturalKey("2008-01-02~Amendment 1")).andReturn(null);
        expect(element.attributeValue("previous-amendment-key")).andReturn("2008-01-01~Amendment 0");
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
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append(format("<amendment name=\"{0}\" date=\"2008-01-02\"", amendment1.getName()))
                .append(format("           mandatory=\"true\" previous-amendment-key=\"{0}\"", amendment1.getPreviousAmendment().getNaturalKey()))
                .append(format("       {0}=\"{1}\""     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
                .append(format("       {0}=\"{1} {2}\""     , SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, SCHEMA_LOCATION))
                .append(format("       {0}=\"{1}\">"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
                .append(       "</amendment>");

        String actual = serializer.createDocumentString(amendment1);

        assertXMLEqual(expected.toString(), actual);
    }
}
