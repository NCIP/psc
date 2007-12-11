package edu.northwestern.bioinformatics.studycalendar.utils.dataloaders;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.io.FileInputStream;
import java.util.List;

/**
 * @author John Dzak
 */
public class ActivityXmlReaderTest extends StudyCalendarTestCase {
    private FileInputStream input;
    private ActivityXmlReader reader;

    protected void setUp() throws Exception {
        super.setUp();

        reader = new ActivityXmlReader();
        input = new FileInputStream("src/test/java/edu/northwestern/bioinformatics/studycalendar/utils/dataloaders/data/ActivityXmlReaderTest.xml");
    }

    public void testParser() throws Exception {

        List<Source> actual = reader.read(input);

        assertEquals("Wrong number of sources", 2, actual.size());

        assertEquals("Wrong number of activities", 2, actual.get(0).getActivities().size());
        assertEquals("Wrong number of activities", 1, actual.get(1).getActivities().size());

        Activity activity0 = actual.get(0).getActivities().get(0);
        assertEquals("Wrong code", "BS1", activity0.getCode());
        assertEquals("Wrong name", "Bone Scan", activity0.getName());
        assertEquals("Wrong source", "ICD-9", activity0.getSource().getName());
        assertEquals("Wrong type", ActivityType.DISEASE_MEASURE, activity0.getType());
        assertEquals("Wrong description", "Scan subjects bones", activity0.getDescription());

        Activity activity1 = actual.get(0).getActivities().get(1);
        assertEquals("Wrong code", "C123", activity1.getCode());
        assertEquals("Wrong name", "Capecitabine", activity1.getName());
        assertEquals("Wrong source", "ICD-9", activity1.getSource().getName());
        assertEquals("Wrong type", ActivityType.INTERVENTION, activity1.getType());
        assertEquals("Wrong description", "Administer Drug Capecitabine", activity1.getDescription());

        Activity activity2 = actual.get(1).getActivities().get(0);
        assertEquals("Wrong code", "CTC1", activity2.getCode());
        assertEquals("Wrong name", "CTC Scan", activity2.getName());
        assertEquals("Wrong source", "LOINK", activity2.getSource().getName());
        assertEquals("Wrong type", ActivityType.DISEASE_MEASURE, activity2.getType());
        assertEquals("Wrong description", "CTC Scan", activity2.getDescription());
    }
}