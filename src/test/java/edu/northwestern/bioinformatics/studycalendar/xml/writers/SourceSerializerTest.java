package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createActivity;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.service.SourceService;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.List;

import static org.easymock.EasyMock.expect;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.IOUtils;

public class SourceSerializerTest extends ControllerTestCase {
    private static final String SOURCE_NAME = "TestSource";
    private SourceSerializer serializer;
    private Source source, anotherSource;
    private Activity act1, act2, act3;
    private static final String CSV_DELIM = ",";
    private static final String XLS_DELIM = "\t";

    private String headerForCSV = "Name,Type,Code,Description,Source,\n";
    private String headerForXLS = "Name\tType\tCode\tDescription\tSource\t\n";
    private FileInputStream valid;

    private SourceDao sourceDao;
    private SourceService sourceService;
    private FileInputStream invalid;
    private FileInputStream invalidFormat;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serializer = new SourceSerializer();
        source = setId(11, createNamedInstance(SOURCE_NAME, Source.class));
        anotherSource = setId(12, createNamedInstance(SOURCE_NAME, Source.class));
        source = createNamedInstance(SOURCE_NAME, Source.class);
        act1 = createActivity("Activity1", "Code1", source, ActivityType.OTHER);
        act2 = createActivity("Activity2", "Code2", source, ActivityType.INTERVENTION);
        act3 = createActivity("Activity3", "Code3", source, ActivityType.OTHER);

        valid = new FileInputStream("src/test/java/edu/northwestern/bioinformatics/studycalendar/xml/writers/data/Activity.csv");

        invalid = new FileInputStream("src/test/java/edu/northwestern/bioinformatics/studycalendar/xml/writers/data/Activity-invalid.csv");
        invalidFormat = new FileInputStream("src/test/java/edu/northwestern/bioinformatics/studycalendar/xml/writers/data/Activity-invalid-format.csv");
        sourceDao = registerDaoMockFor(SourceDao.class);
        sourceService = new SourceService();
        sourceService.setSourceDao(sourceDao);
        serializer.setSourceDao(sourceDao);
        serializer.setSourceService(sourceService);

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
        expect(sourceDao.getByName(anotherSource.getName())).andReturn(anotherSource).anyTimes();
        sourceDao.save(anotherSource);
        replayMocks();

        Source importedSource = serializer.readDocument(valid);
        verifyMocks();
        assertNotNull("sources must not be null", importedSource);

        List<Activity> activities = importedSource.getActivities();
        assertEquals("Document contains 3 activities", Integer.valueOf(3), Integer.valueOf(activities.size()));

        for (Activity activity : activities) {
            if (StringUtils.equals(activity.getName(), act1.getName())) {
                validateActivity(activity, act1);
            } else if (StringUtils.equals(activity.getName(), act2.getName())) {

            } else if (StringUtils.equals(activity.getName(), act3.getName())) {

            } else {
                assertFalse("Activity must exists in source" + activity.toString(), true);
            }
        }

    }


    public void testExportAndImportCSV() throws Exception {

        expect(sourceDao.getByName(anotherSource.getName())).andReturn(anotherSource).anyTimes();

        replayMocks();
        String document = serializer.createDocumentString(source, CSV_DELIM);
        verifyMocks();

        resetMocks();
        expect(sourceDao.getByName(anotherSource.getName())).andReturn(anotherSource).anyTimes();
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
                validateActivity(activity, act1);
            } else if (StringUtils.equals(activity.getName(), act2.getName())) {

            } else if (StringUtils.equals(activity.getName(), act3.getName())) {

            } else {
                assertFalse("Activity must exists in source" + activity.toString(), true);
            }
        }


    }

    public void testActivitiesMustBelongToSameSingleSourceInImportCSV() throws Exception {

        expect(sourceDao.getByName(anotherSource.getName())).andReturn(anotherSource).anyTimes();
        replayMocks();

        try {
            serializer.readDocument(invalid);
            fail("All activities must belong to same source. TestSource and another source are not same source.");
        } catch (Exception e) {
            assertEquals("All activities must belong to same source. TestSource and another source are not same source.", e.getMessage());
        }
        verifyMocks();


    }


    public void testImportCSVForNullActivityType() throws Exception {

        expect(sourceDao.getByName(anotherSource.getName())).andReturn(anotherSource).anyTimes();
        replayMocks();

        try {
            serializer.readDocument(invalidFormat);
            fail("Activity type invalid type either does not exists or it is null. Please choose [Disease Measure, Intervention, Lab Test, Procedure, Other] activity type only.");
        } catch (Exception e) {
            assertEquals("Activity type invalid type either does not exists or it is null. Please choose [Disease Measure, Intervention, Lab Test, Procedure, Other] activity type only.", e.getMessage());

        }
        verifyMocks();


    }

    private void validateActivity(Activity activity, Activity act1) {
        assertEquals("name must be same", activity.getName(), act1.getName());
        assertEquals("source must be same", activity.getSource(), act1.getSource());
        assertEquals("code must be same", activity.getCode(), act1.getCode());
        assertEquals("desc must be same", activity.getDescription(), act1.getDescription());
        assertEquals("type must be same", activity.getType(), act1.getType());
    }
}
