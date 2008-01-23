package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import java.util.Calendar;

public class AmendmentXmlSerializerTest extends StudyCalendarXmlTestCase {
    private AmendmentDao amendmentDao;
    private AmendmentXmlSerializer serializer;
    private Amendment amendment1;
    private Element element;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);

        Study study = createNamedInstance("Study A", Study.class);
        serializer = new AmendmentXmlSerializer(study);
        serializer.setAmendmentDao(amendmentDao);

        Amendment amendment0 = setGridId("grid0", new Amendment());

        amendment1 = setGridId("grid1", new Amendment());
        amendment1.setMandatory(true);
        amendment1.setName("Amendment 1");
        amendment1.setPreviousAmendment(amendment0);
        amendment1.setDate(DateUtils.createDate(2008, Calendar.JANUARY, 2));
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(amendment1);

        assertEquals("Wrong attribute size", 5, actual.attributeCount());
        assertEquals("Wrong grid id", "grid1", actual.attributeValue("id"));
        assertEquals("Wrong name", "Amendment 1", actual.attributeValue("name"));
        assertEquals("Wrong date", "2008-01-02", actual.attributeValue("date"));
        assertEquals("Wrong date", "true", actual.attributeValue("mandatory"));
        assertEquals("Wrong previous amdendment id", "2008-01-02~Amendment 1", actual.attributeValue("previous-amendment-key"));
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
        replayMocks();

        Amendment actual = serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong name", "Amendment 1", actual.getName());
    }
}
