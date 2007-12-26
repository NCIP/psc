package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;

import static java.text.MessageFormat.format;

public class StudyXMLReaderTest extends StudyCalendarTestCase {
    Study study;

    protected void setUp() throws Exception {
        super.setUp();

        //reader = new StudyXMLReader();
    }

    public void testCreateStudy() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid1\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_NAMESPACE))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XML_SCHEMA))
           .append(       "</study>");

        //study = reader.

        //assertEquals("Wrong Study Identifier", "Study A", study.getAssignedIdentifier());
        //assertEquals("Wrong Grid Id", "grid1", study.getGridId());
    }
}
