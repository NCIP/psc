package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import edu.nwu.bioinformatics.commons.DateUtils;
import static org.easymock.EasyMock.expect;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import static java.text.MessageFormat.format;
import java.util.List;
import java.util.Calendar;

public class StudyXMLReaderTest extends StudyCalendarTestCase {
    private StudyXMLReader reader;
    private StudyDao studyDao;
    private PlannedCalendarDao plannedCalendarDao;
    private AmendmentDao amendmentDao;

    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        plannedCalendarDao = registerDaoMockFor(PlannedCalendarDao.class);

        reader = new StudyXMLReader();
        reader.setStudyDao(studyDao);
        reader.setAmendmentDao(amendmentDao);
        reader.setPlannedCalendarDao(plannedCalendarDao);
    }

    public void testReadNewStudy() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid1\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_NAMESPACE))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XML_SCHEMA))
           .append(       "</study>");

        expect(studyDao.getByGridId("grid1")).andReturn(null);
        replayMocks();

        Study actual = reader.parseStudy(getDocument(buf));
        verifyMocks();

        assertEquals("Wrong Study Identifier", "Study A", actual.getAssignedIdentifier());
        assertEquals("Wrong Grid Id", "grid1", actual.getGridId());
    }

    public void testReadExistingStudy() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid1\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_NAMESPACE))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XML_SCHEMA))
           .append(       "</study>");

        Study study = createNamedInstance("Study A", Study.class);
        study.setGridId("grid1");
        
        expect(studyDao.getByGridId("grid1")).andReturn(study);
        replayMocks();
        
        Study actual = reader.parseStudy(getDocument(buf));
        verifyMocks();

        assertSame("Studies should be same", study, actual);
        assertEquals("Wrong Study Identifier", "Study A", actual.getAssignedIdentifier());
        assertEquals("Wrong Grid Id", "grid1", actual.getGridId());
    }

    public void testReadNewPlannedCalendar() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<planned-calendar id=\"grid1\" />\n");

        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(null);
        replayMocks();
        
        PlannedCalendar actual = reader.parsePlannedCalendar(getDocument(buf));
        verifyMocks();

        assertEquals("Wrong Grid Id", "grid1", actual.getGridId());
    }

    public void testReadExistingPlannedCalendar() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<planned-calendar id=\"grid1\" />\n");

        PlannedCalendar calendar = new PlannedCalendar();
        calendar.setGridId("grid1");

        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);
        replayMocks();

        PlannedCalendar actual = reader.parsePlannedCalendar(getDocument(buf));
        verifyMocks();

        assertSame("Studies should be same", calendar, actual);
        assertEquals("Wrong Grid Id", "grid1", actual.getGridId());
    }

    public void testReadAmendments() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study>")
           .append(       "  <amendment id=\"grid1\" name=\"amendment A\" date=\"2007-12-25\" mandatory=\"true\"/>\n")
           .append(       "  <amendment id=\"grid2\" name=\"amendment B\" date=\"2007-12-26\" mandatory=\"true\" previous-amendment-id=\"grid1\"/>\n")
           .append(       "</study>");

        Amendment amendment0 = new Amendment();
        amendment0.setGridId("grid1");
        amendment0.setName("amendment A");
        amendment0.setDate(DateUtils.createDate(2007, Calendar.DECEMBER, 25, 0, 0, 0));
        amendment0.setMandatory(true);

        expect(amendmentDao.getByGridId("grid1")).andReturn(amendment0);
        expect(amendmentDao.getByGridId("grid2")).andReturn(null);
        replayMocks();

        List<Amendment> actual = reader.parseAmendment(getDocument(buf));
        verifyMocks();

        Amendment actualAmendment0 = actual.get(0);
        assertSame("Amendments Should be the same", amendment0, actualAmendment0);
        assertEquals("Wrong Grid Id", "grid1", actualAmendment0.getGridId());
        assertEquals("Wrong Name", "amendment A", actualAmendment0.getName());
        assertEquals("Wrong Date", DateUtils.createDate(2007, Calendar.DECEMBER, 25, 0, 0, 0), actualAmendment0.getDate());
        assertTrue("Wrong Mandatory Value", actualAmendment0.isMandatory());
        assertNull("Previous Amendment Should be null", actualAmendment0.getPreviousAmendment());

        Amendment actualAmendment1 = actual.get(1);
        assertEquals("Wrong Grid Id", "grid2", actualAmendment1.getGridId());
        assertEquals("Wrong Name", "amendment B", actualAmendment1.getName());
        assertEquals("Wrong Date", DateUtils.createDate(2007, Calendar.DECEMBER, 26, 0, 0, 0), actualAmendment1.getDate());
        assertTrue("Wrong Mandatory Value", actualAmendment1.isMandatory());
        assertSame("Previous Amendment Should be same", amendment0, actualAmendment1.getPreviousAmendment());

    }


    /* Test Helpers */
    private Document getDocument(StringBuffer buf) throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(buf.toString().getBytes());

        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        //Using factory get an instance of document builder
        DocumentBuilder db = dbf.newDocumentBuilder();

        //parse using builder to get DOM representation of the XML file
        return db.parse(input);

    }
}
