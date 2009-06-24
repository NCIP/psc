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
    private static final char CSV_DELIM = ',';
    private static final char XLS_DELIM = '\t';
    private static final String CSV_HEADER = "Name,Type,Code,Description,Source";
    private static final String XLS_HEADER = "Name\tType\tCode\tDescription\tSource";

    private ActivityType other, intervention;

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

        other = Fixtures.createActivityType("OTHER");
        intervention = Fixtures.createActivityType("INTERVENTION");

        act1 = createActivity("Activity1", "Code1", source, other);
        act2 = createActivity("Activity2", "Code2", source, intervention);
        act2.setDescription("A2 also has frob, bar, and zap");
        act3 = createActivity("Activity3", "Code3", source, other);
        act3.setDescription("A3\nhas a long\ndescription");

        dataDir = getModuleRelativeDirectory("core", "src/test/java/edu/northwestern/bioinformatics/studycalendar/xml/writers/data");

        sourceDao = registerDaoMockFor(SourceDao.class);
        sourceService = new SourceService();
        sourceService.setSourceDao(sourceDao);
        serializer.setSourceDao(sourceDao);
        serializer.setSourceService(sourceService);
        serializer.setActivityTypeDao(activityTypeDao);
    }

    public void testCsvForActivityWithDescriptionWithCommas() throws Exception {
        Source oneAct = Fixtures.createSource("Src", act2);
        String doc = serializer.createDocumentString(oneAct, CSV_DELIM);
        String expected = CSV_HEADER + "\nActivity2,INTERVENTION,Code2,\"A2 also has frob, bar, and zap\",Src\n";
        assertEquals(expected, doc);
    }

    public void testCsvForActivityWithDescriptionWithNewlines() throws Exception {
        Source oneAct = Fixtures.createSource("Src", act3);
        String doc = serializer.createDocumentString(oneAct, CSV_DELIM);
        String expected = CSV_HEADER + "\nActivity3,OTHER,Code3,\"A3\nhas a long\ndescription\",Src\n";
        assertEquals(expected, doc);
    }

    public void testWillUseOtherDelimiter() throws Exception {
        String document = serializer.createDocumentString(source, XLS_DELIM);
        String[] rows = document.split("\n");
        assertEquals("Header incorrect", XLS_HEADER, rows[0]);
        assertEquals("Wrong first row", "Activity1\tOTHER\tCode1\t\tTestSource", rows[1]);
    }

    public void testCsvWithMultipleRows() throws Exception {
        String doc = serializer.createDocumentString(createSource("foo",
            createActivity("A"),
            createActivity("B"),
            createActivity("C"),
            createActivity("D")
        ), CSV_DELIM);
        assertEquals("Wrong number of rows:\n" + doc, 4 + 1, doc.split("\n").length);
    }

    public void testReadCSV() throws Exception {
        InputStream validDocStream = new FileInputStream(new File(dataDir, "Activity.csv"));

        expect(sourceDao.getByName(anotherSource.getName())).andReturn(anotherSource).anyTimes();
        expect(activityTypeDao.getByName("Other")).andReturn(other).anyTimes();
        expect(activityTypeDao.getByName("Intervention")).andReturn(intervention).anyTimes();
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

        expect(activityTypeDao.getByName("OTHER")).andReturn(other).anyTimes();
        expect(activityTypeDao.getByName("INTERVENTION")).andReturn(intervention).anyTimes();
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
        expect(activityTypeDao.getByName("Other")).andReturn(other).anyTimes();
        expect(activityTypeDao.getByName("Intervention")).andReturn(other).anyTimes();
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
