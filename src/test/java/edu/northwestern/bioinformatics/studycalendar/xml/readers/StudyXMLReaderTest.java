package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import static org.easymock.EasyMock.expect;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import static java.text.MessageFormat.format;

public class StudyXMLReaderTest extends StudyCalendarTestCase {
    private StudyXMLReader reader;
    private StudyDao studyDao;

    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        reader = new StudyXMLReader(studyDao);
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

        Study actual = reader.parseStudy(parse(buf));
        verifyMocks();

        assertEquals("Wrong Study Identifier", "Study A", actual.getAssignedIdentifier());
        assertEquals("Wrong Grid Id", "grid1", actual.getGridId());
    }

    public void testReadExistingStudy() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid2\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_NAMESPACE))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XML_SCHEMA))
           .append(       "</study>");

        Study study = createNamedInstance("Study A", Study.class);
        study.setGridId("grid2");
        study.setAssignedIdentifier("Study A");
        
        expect(studyDao.getByGridId("grid2")).andReturn(study);
        replayMocks();
        
        Study actual = reader.parseStudy(parse(buf));
        verifyMocks();

        assertSame("Studies should be same", study, actual);
        assertEquals("Wrong Study Identifier", "Study A", actual.getAssignedIdentifier());
        assertEquals("Wrong Grid Id", "grid2", actual.getGridId());
    }

    /* Test Helpers */
    private Document parse(StringBuffer buf) throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(buf.toString().getBytes());

        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        //Using factory get an instance of document builder
        DocumentBuilder db = dbf.newDocumentBuilder();

        //parse using builder to get DOM representation of the XML file
        return db.parse(input);

    }
}
