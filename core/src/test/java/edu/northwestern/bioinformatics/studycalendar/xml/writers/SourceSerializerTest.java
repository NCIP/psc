package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.service.SourceService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import static org.easymock.EasyMock.expect;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class SourceSerializerTest extends StudyCalendarTestCase {
    private static final String SOURCE_NAME = "TestSource";
    private SourceSerializer serializer;
    private Source source, anotherSource;
    private Activity act1, act2, act3;
    private static final String CSV_DELIM = ",";
    private static final String XLS_DELIM = "\t";

    private ActivityType activityType1, activityType2;

    private String headerForCSV = "Name,Type,Code,Description,Source,\n";
    private String headerForXLS = "Name\tType\tCode\tDescription\tSource\t\n";

    private SourceDao sourceDao;
    private SourceService sourceService;
    private ActivityTypeDao activityTypeDao;
    private File dataDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serializer = new SourceSerializer();
        source = setId(11, createNamedInstance(SOURCE_NAME, Source.class));
        anotherSource = setId(12, createNamedInstance(SOURCE_NAME, Source.class));
        source = createNamedInstance(SOURCE_NAME, Source.class);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);

        activityType1 = Fixtures.createActivityType("OTHER");
        activityType2 = Fixtures.createActivityType("INTERVENTION");

        act1 = createActivity("Activity1", "Code1", source, activityType1);
        act2 = createActivity("Activity2", "Code2", source, activityType2);
        act3 = createActivity("Activity3", "Code3", source, activityType1);

        dataDir = getModuleRelativeDirectory("core", "src/test/java/edu/northwestern/bioinformatics/studycalendar/xml/writers/data");

        sourceDao = registerDaoMockFor(SourceDao.class);
        sourceService = new SourceService();
        sourceService.setSourceDao(sourceDao);
        serializer.setSourceDao(sourceDao);
        serializer.setSourceService(sourceService);
        serializer.setActivityTypeDao(activityTypeDao);
    }

    public void testCreateHeaderForCSV() throws Exception {
        String header = serializer.constructHeader(CSV_DELIM);
        assertEquals("Wrong header for csv", headerForCSV, header);
    }

    public void testCreateHeaderForXLS() throws Exception {
        String header = serializer.constructHeader(XLS_DELIM);
        assertEquals("Wrong header for csv", headerForXLS, header);
    }


    public void testCreateCSV() throws Exception {
        String document = serializer.createDocumentString(source, CSV_DELIM);
        assertTrue("Document contains header", document.contains(headerForCSV));
        assertTrue("Document contains activity1", document.contains(act1.getName()));
        assertTrue("Document contains activity2", document.contains(act2.getType().getName()));
        assertTrue("Document contains activity3", document.contains(act3.getCode()));
        assertTrue("Document contains CSV delimiter", document.contains(CSV_DELIM));
    }

    public void testCreateXLS() throws Exception {
        String document = serializer.createDocumentString(source, XLS_DELIM);
        assertTrue("Document contains header", document.contains(headerForXLS));
        assertTrue("Document contains activity1", document.contains(act1.getName()));
        assertTrue("Document contains activity2", document.contains(act2.getType().getName()));
        assertTrue("Document contains activity3", document.contains(act3.getCode()));
        assertTrue("Document contains XLS delimiter", document.contains(XLS_DELIM));
    }

    public void testReadCSV() throws Exception {
        InputStream validDocStream = new FileInputStream(new File(dataDir, "Activity.csv"));

        expect(sourceDao.getByName(anotherSource.getName())).andReturn(anotherSource).anyTimes();
        expect(activityTypeDao.getByName("Other")).andReturn(activityType1).anyTimes();
        expect(activityTypeDao.getByName("Intervention")).andReturn(activityType2).anyTimes();
        sourceDao.save(anotherSource);
        replayMocks();

        Source importedSource = serializer.readDocument(validDocStream);
        verifyMocks();
        assertNotNull("sources must not be null", importedSource);

        List<Activity> activities = importedSource.getActivities();
        assertEquals("Document contains 3 activities", Integer.valueOf(3), Integer.valueOf(activities.size()));

        for (Activity activity : activities) {
            if (StringUtils.equals(activity.getName(), act1.getName())) {
                assertActivitiesEqual(act1, activity);
            } else if (StringUtils.equals(activity.getName(), act2.getName())) {
                assertActivitiesEqual(act2, activity);
            } else if (StringUtils.equals(activity.getName(), act3.getName())) {
                assertActivitiesEqual(act3, activity);
            } else {
                assertFalse("Activity must exists in source" + activity.toString(), true);
            }
        }
    }


    public void testExportAndImportCSV() throws Exception {
        expect(sourceDao.getByName(anotherSource.getName())).andReturn(anotherSource).anyTimes();

        expect(activityTypeDao.getByName("OTHER")).andReturn(activityType1).anyTimes();
        expect(activityTypeDao.getByName("INTERVENTION")).andReturn(activityType2).anyTimes();
        String document = serializer.createDocumentString(source, CSV_DELIM);

        sourceDao.save(anotherSource);

        replayMocks();
        Source importedSource = serializer.readDocument(IOUtils.toInputStream(document));
        verifyMocks();

        assertNotNull("sources must not be null", importedSource);
        assertEquals("import should be able to import file that was created by calling export method ", source, importedSource);
        List<Activity> activities = importedSource.getActivities();
        assertEquals("Document contains 3 activities", Integer.valueOf(3), Integer.valueOf(activities.size()));

        for (Activity activity : activities) {
            if (StringUtils.equals(activity.getName(), act1.getName())) {
                assertActivitiesEqual(act1, activity);
            } else if (StringUtils.equals(activity.getName(), act2.getName())) {
                assertActivitiesEqual(act2, activity);
            } else if (StringUtils.equals(activity.getName(), act3.getName())) {
                assertActivitiesEqual(act3, activity);
            } else {
                assertFalse("Activity must exists in source" + activity.toString(), true);
            }
        }
    }

    public void testActivitiesMustBelongToSameSingleSourceInImportCSV() throws Exception {
        InputStream multipleSourceDoc = new FileInputStream(new File(dataDir, "Activity-multiple-sources.csv"));

        expect(sourceDao.getByName(anotherSource.getName())).andReturn(anotherSource).anyTimes();
        expect(activityTypeDao.getByName("Other")).andReturn(activityType1).anyTimes();
        expect(activityTypeDao.getByName("Intervention")).andReturn(activityType1).anyTimes();
        replayMocks();

        try {
            serializer.readDocument(multipleSourceDoc);
            fail("All activities must belong to same source. TestSource and another source are not same source.");
        } catch (Exception e) {
            assertEquals("All activities must belong to same source. TestSource and another source are not same source.", e.getMessage());
        }
        verifyMocks();
    }

    private void assertActivitiesEqual(Activity expected, Activity actual) {
        assertEquals("name must be same", expected.getName(), actual.getName());
        assertEquals("source must be same", expected.getSource(), actual.getSource());
        assertEquals("code must be same", expected.getCode(), actual.getCode());
        assertEquals("desc must be same", expected.getDescription(), actual.getDescription());
        assertEquals("type must be same", expected.getType(), actual.getType());
    }
}
