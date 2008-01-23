package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.dom4j.Element;

import java.util.Calendar;

public class AmendmentXmlSerializerTest extends StudyCalendarXmlTestCase {
    private AmendmentDao amendmentDao;
    private AmendmentXmlSerializer serializer;
    private Amendment amendment1;

    protected void setUp() throws Exception {
        super.setUp();

        amendmentDao = registerDaoMockFor(AmendmentDao.class);

        serializer = new AmendmentXmlSerializer();
        serializer.setAmendmentDao(amendmentDao);

        Amendment amendment0 = setGridId("grid0", new Amendment());

        amendment1 = setGridId("grid1", new Amendment());
        amendment1.setName("Amendment 1");
        amendment1.setDate(DateUtils.createDate(2008, Calendar.JANUARY, 2));
        amendment1.setMandatory(true);
        amendment1.setPreviousAmendment(amendment0);
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(amendment1);

        assertEquals("Wrong attribute size", 5, actual.attributeCount());
        assertEquals("Wrong grid id", "grid1", actual.attributeValue("id"));
        assertEquals("Wrong name", "Amendment 1", actual.attributeValue("name"));
        assertEquals("Wrong date", "2008-01-02", actual.attributeValue("date"));
        assertEquals("Wrong date", "true", actual.attributeValue("mandatory"));
        assertEquals("Wrong previous amenmdnet id", "grid0", actual.attributeValue("previous-amendment-id"));
    }
}
