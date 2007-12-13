package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;

import java.io.InputStream;
import static java.util.Arrays.asList;
import java.util.List;

/**
 * @author John Dzak
 */
public class MultipartFileActivityLoaderTest extends StudyCalendarTestCase {
    private MultipartFileActivityLoader loader;
    private SourceDao sourceDao;
    private ActivityXmlReader activityXmlReader;
    private Source source0, source1;
    private List<Source> sources;

    protected void setUp() throws Exception {
        super.setUp();

        activityXmlReader = registerMockFor(ActivityXmlReader.class);
        sourceDao = registerDaoMockFor(SourceDao.class);

        loader = new MultipartFileActivityLoader();
        loader.setSourceDao(sourceDao);
        loader.setActivityXmlReader(activityXmlReader);

        source0 = createNamedInstance("ICD-9", Source.class);
        source1 = createNamedInstance("Loink", Source.class);
        sources = asList(source0, source1);
    }

    public void testSave() {
        sourceDao.save(source0);
        sourceDao.save(source1);
        replayMocks();

        loader.save(sources);
        verifyMocks();
    }

    public void testReadData() throws Exception {
        InputStream stream = null;

        expect(activityXmlReader.read(stream)).andReturn(sources);
        replayMocks();

        loader.readData(stream);
        verifyMocks();
    }

    public void testReplaceCollidingSources() throws Exception {
        Source existingSource = setId(0, createNamedInstance("ICD-9", Source.class));

        Activity activity0 = assignSource(createActivity("Bone Scan"), source0);
        Activity activity1 = assignSource(createActivity("CTC Scan"), source1);

        List<Source> existingSources = asList(existingSource);

        expect(sourceDao.getAll()).andReturn(existingSources);
        replayMocks();

        List<Source> actualSources = loader.replaceCollidingSources(sources);
        verifyMocks();

        Source actualSource0 = actualSources.get(0);
        Source actualSource1 = actualSources.get(1);

        assertEquals("Wrong Id", 0, (int) actualSource0.getId());
        assertNull("Id should be null", actualSource1.getId());

        assertEquals("Wrong number of activities", 1, actualSource0.getActivities().size());
        assertEquals("Wrong number of activities", 1, actualSource1.getActivities().size());

        assertEquals("Wrong activity", activity0.getName(), actualSource0.getActivities().get(0).getName());
        assertEquals("Wrong activity", activity1.getName(), actualSource1.getActivities().get(0).getName());
    }

    private Activity assignSource(Activity activity, Source source) {
        activity.setSource(source);
        source.getActivities().add(activity);
        return activity;
    }
}
