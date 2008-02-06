package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Element;

/**
 * @author John Dzak
 */
public class StudySiteXmlSerializerTest extends StudyCalendarXmlTestCase {
    private StudySiteXmlSerializer serializer;
    private StudySite studySite;

    protected void setUp() throws Exception {
        super.setUp();

        serializer = new StudySiteXmlSerializer();

        Study study = createNamedInstance("Cancer Study", Study.class);
        Site site = createNamedInstance("Northwestern University", Site.class);

        studySite = Fixtures.createStudySite(study, site);
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(studySite);

        assertEquals("Wrong study name", "Cancer Study", actual.attributeValue("study-name"));
        assertEquals("Wrong site name", "Northwestern University", actual.attributeValue("site-name"));
    }

//    public void testReadElement() {
//        throw new UnsupportedOperationException();
//    }
}
