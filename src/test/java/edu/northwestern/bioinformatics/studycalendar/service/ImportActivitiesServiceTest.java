package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import static edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ActivitySourceXmlSerializer;
import static org.easymock.EasyMock.expect;

import java.io.InputStream;
import static java.util.Arrays.asList;
import java.util.List;

/**
 * @author John Dzak
 */
public class ImportActivitiesServiceTest extends StudyCalendarTestCase {
    private ImportActivitiesService service;
    private SourceDao sourceDao;
    private ActivitySourceXmlSerializer serializer;
    private Source source0, source1;
    private List<Source> sources;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        serializer = registerMockFor(ActivitySourceXmlSerializer.class);
        sourceDao = registerDaoMockFor(SourceDao.class);

        service = new ImportActivitiesService();
        service.setSourceDao(sourceDao);
        service.setXmlSerializer(serializer);

        source0 = createNamedInstance("ICD-9", Source.class);
        source1 = createNamedInstance("Loink", Source.class);
        sources = asList(source0, source1);
    }

    public void testSave() {
        sourceDao.save(source0);
        sourceDao.save(source1);
        replayMocks();

        service.save(sources);
        verifyMocks();
    }

    public void testRead() throws Exception {
        InputStream target = registerMockFor(InputStream.class);
        expect(serializer.readCollectionOrSingleDocument(target)).andReturn(sources);
        replayMocks();
        service.readData(target);
        verifyMocks();
    }

    public void testReplaceCollidingSources() throws Exception {
        Source existingSource = setId(0, createNamedInstance("ICD-9", Source.class));

        Activity activity0 = assignSource(createActivity("Bone Scan"), source0);
        Activity activity1 = assignSource(createActivity("CTC Scan"), source1);

        List<Source> existingSources = asList(existingSource);

        expect(sourceDao.getAll()).andReturn(existingSources);
        replayMocks();

        List<Source> actualSources = service.replaceCollidingSources(sources);
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

