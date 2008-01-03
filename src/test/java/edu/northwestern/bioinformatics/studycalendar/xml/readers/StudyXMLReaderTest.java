package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import static java.util.Arrays.asList;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import edu.northwestern.bioinformatics.studycalendar.xml.validators.Schema;
import edu.nwu.bioinformatics.commons.DateUtils;
import static org.easymock.EasyMock.expect;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.validation.SchemaFactory;
import javax.xml.XMLConstants;
import java.io.ByteArrayInputStream;
import java.io.File;
import static java.text.MessageFormat.format;
import java.util.List;
import java.util.Calendar;
import java.net.URL;

public class StudyXMLReaderTest extends StudyCalendarTestCase {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private StudyXMLReader reader;
    private StudyDao studyDao;
    private PlannedCalendarDao plannedCalendarDao;
    private AmendmentDao amendmentDao;
    private EpochDao epochDao;
    private DeltaDao deltaDao;

    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        epochDao = registerDaoMockFor(EpochDao.class);
        deltaDao = registerDaoMockFor(DeltaDao.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        plannedCalendarDao = registerDaoMockFor(PlannedCalendarDao.class);

        reader = new StudyXMLReader();
        reader.setStudyDao(studyDao);
        reader.setEpochDao(epochDao);
        reader.setDeltaDao(deltaDao);
        reader.setAmendmentDao(amendmentDao);
        reader.setPlannedCalendarDao(plannedCalendarDao);
    }

    public void testReadNewStudy() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid0\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_NAMESPACE))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XML_SCHEMA))
           .append(       " <planned-calendar id=\"grid1\" />\n")
           .append(       "</study>");

        expect(studyDao.getByGridId("grid0")).andReturn(null);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(null);
        replayMocks();

        Study actual = reader.parseStudy(getDocument(buf));
        verifyMocks();

        assertEquals("Wrong Study Identifier", "Study A", actual.getAssignedIdentifier());
        assertEquals("Wrong Grid Id", "grid0", actual.getGridId());
    }

    public void testReadExistingStudy() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid0\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_NAMESPACE))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XML_SCHEMA))
           .append(       " <planned-calendar id=\"grid1\" />\n")
           .append(       "</study>");

        Study study = createNamedInstance("Study A", Study.class);
        study.setGridId("grid0");
        
        expect(studyDao.getByGridId("grid0")).andReturn(study);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(null);
        replayMocks();
        
        Study actual = reader.parseStudy(getDocument(buf));
        verifyMocks();

        assertSame("Studies should be same", study, actual);
        assertEquals("Wrong Study Identifier", "Study A", actual.getAssignedIdentifier());
        assertEquals("Wrong Grid Id", "grid0", actual.getGridId());
    }

    // TODO: When new Planned Calendar check relation to study
    public void testReadNewPlannedCalendar() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append(       "<study assigned-identifier=\"Study A\" id=\"grid0\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_NAMESPACE))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XML_SCHEMA))
           .append(       " <planned-calendar id=\"grid1\" />\n")
           .append(       "</study>");

        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(null);
        replayMocks();
        
        PlannedCalendar actual = reader.parsePlannedCalendar(getDocument(buf), createNamedInstance("Study A", Study.class));
        verifyMocks();

        assertEquals("Wrong Grid Id", "grid1", actual.getGridId());
    }

    public void testReadExistingPlannedCalendar() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid0\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_NAMESPACE))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XML_SCHEMA))
           .append(       " <planned-calendar id=\"grid1\" />\n")
           .append(       "</study>");

        PlannedCalendar calendar = new PlannedCalendar();
        calendar.setGridId("grid1");

        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);
        replayMocks();

        PlannedCalendar actual = reader.parsePlannedCalendar(getDocument(buf), createNamedInstance("Study A", Study.class));
        verifyMocks();

        assertSame("Studies should be same", calendar, actual);
        assertEquals("Wrong Grid Id", "grid1", actual.getGridId());
    }

    // TODO: When new Amendment check relation to study
    public void testReadAmendments() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid0\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_NAMESPACE))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XML_SCHEMA))
           .append(       "  <planned-calendar id=\"grid1\" />\n")
           .append(       "  <amendment id=\"grid2\" name=\"amendment A\" date=\"2007-12-25\" mandatory=\"true\"/>\n")
           .append(       "  <amendment id=\"grid3\" name=\"amendment B\" date=\"2007-12-26\" mandatory=\"true\" previous-amendment-id=\"grid2\"/>\n")
           .append(       "</study>");

        Amendment amendment0 = new Amendment();
        amendment0.setGridId("grid2");
        amendment0.setName("amendment A");
        amendment0.setDate(DateUtils.createDate(2007, Calendar.DECEMBER, 25, 0, 0, 0));
        amendment0.setMandatory(true);

        expect(amendmentDao.getByGridId("grid2")).andReturn(amendment0);
        expect(amendmentDao.getByGridId("grid3")).andReturn(null);
        replayMocks();

        List<Amendment> actual = reader.parseAmendment(getDocument(buf), new Study());
        verifyMocks();

        Amendment actualAmendment0 = actual.get(0);
        assertSame("Amendments Should be the same", amendment0, actualAmendment0);
        assertEquals("Wrong Grid Id", "grid2", actualAmendment0.getGridId());
        assertEquals("Wrong Name", "amendment A", actualAmendment0.getName());
        assertEquals("Wrong Date", DateUtils.createDate(2007, Calendar.DECEMBER, 25, 0, 0, 0), actualAmendment0.getDate());
        assertTrue("Wrong Mandatory Value", actualAmendment0.isMandatory());
        assertNull("Previous Amendment Should be null", actualAmendment0.getPreviousAmendment());

        Amendment actualAmendment1 = actual.get(1);
        assertEquals("Wrong Grid Id", "grid3", actualAmendment1.getGridId());
        assertEquals("Wrong Name", "amendment B", actualAmendment1.getName());
        assertEquals("Wrong Date", DateUtils.createDate(2007, Calendar.DECEMBER, 26, 0, 0, 0), actualAmendment1.getDate());
        assertTrue("Wrong Mandatory Value", actualAmendment1.isMandatory());
        assertSame("Previous Amendment Should be same", amendment0, actualAmendment1.getPreviousAmendment());

    }

    public void testReadExistingDelta() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid0\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_NAMESPACE))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XML_SCHEMA))
           .append(       "  <planned-calendar id=\"grid1\" />\n")
           .append(       "  <amendment id=\"grid2\" name=\"amendment A\" date=\"2007-12-25\" mandatory=\"true\">\n")
           .append(       "    <delta id=\"grid3\" node-id=\"grid1\">\n")
           .append(       "      <add id=\"grid4\" index=\"0\">")
           .append(       "        <epoch id=\"grid5\" name=\"Epoch A\"/>")
           .append(       "      </add>")
           .append(       "    </delta>")
           .append(       "  </amendment>")
           .append(       "</study>");

        Epoch epoch = createNamedInstance("Epoch A", Epoch.class);
        epoch.setGridId("grid5");

        Add add = Add.create(epoch, 0);

        Delta delta = Delta.createDeltaFor(epoch, add);
        delta.setGridId("grid3");

        Amendment amendment = new Amendment();
        amendment.setGridId("grid2");

        expect(deltaDao.getByGridId("grid3")).andReturn(delta);
        replayMocks();

        List<Delta> actual = reader.parseDeltas(getDocument(buf), amendment);
        verifyMocks();

        Delta actualDelta0 = actual.get(0);
        assertSame("Deltas should be the same", delta, actualDelta0);
        assertSame("Changes should be the same", add, actualDelta0.getChanges().get(0));
        assertSame("Child should be the same", epoch, ((ChildrenChange) actualDelta0.getChanges().get(0)).getChild());
    }

    public void testReadNewDelta() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\"?>\n")
           .append(       "<study id=\"grid0\" assigned-identifier=\"Study A\"")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_NAMESPACE))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XML_SCHEMA))
           .append(       "  <planned-calendar id=\"grid1\"/>\n")
           .append(       "  <amendment id=\"grid2\" name=\"Amendment A\" date=\"2008-01-01\" mandatory=\"true\">\n")
           .append(       "    <delta id=\"grid3\" node-id=\"grid1\">\n")
           .append(       "      <add id=\"grid4\" index=\"0\">\n")
           .append(       "        <epoch id=\"grid5\" name=\"Epoch A\"/>\n")
           .append(       "      </add>\n")
           .append(       "    </delta>\n")
           .append(       "  </amendment>\n")
           .append(       "</study>");

        PlannedCalendar calendar = new PlannedCalendar();
        calendar.setGridId("grid1");

        Amendment amendment = new Amendment();
        amendment.setGridId("grid2");

        expect(deltaDao.getByGridId("grid3")).andReturn(null);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);
        replayMocks();

        List<Delta> actual = reader.parseDeltas(getDocument(buf), amendment);
        verifyMocks();

        Delta actualDelta0 = actual.get(0);
        assertEquals("Wrong grid id", "grid3", actualDelta0.getGridId());
        assertEquals("Wrong node grid id", "grid1", actualDelta0.getNode().getGridId());
    }


    /* Test Helpers */
    private Document getDocument(StringBuffer buf) throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(buf.toString().getBytes());

        // create a SchemaFactory that conforms to W3C XML Schema
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        javax.xml.validation.Schema schema = sf.newSchema(Schema.template.file());

        // get a DOM factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        // configure the factory
        dbf.setNamespaceAware(true);

        // set schema on the factory
        dbf.setSchema(schema);

        // create a new parser that validates documents against
        // the schema
        DocumentBuilder db = dbf.newDocumentBuilder();

        return db.parse(input);
    }

}
