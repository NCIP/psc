package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createActivity;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;

public class SourceSerializerTest extends ControllerTestCase {
    private static final String SOURCE_NAME = "TestSource";
    private SourceSerializer serializer;
    private Source source;
    private Activity act1, act2, act3;
    private static final String CSV_DELIM = ",";
    private static final String XLS_DELIM = "\t";

    private String headerForCSV ="Name,Type,Code,Description,Source,\n";
    private String headerForXLS ="Name\tType\tCode\tDescription\tSource\t\n";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serializer = new SourceSerializer();
        source = setId(11, createNamedInstance(SOURCE_NAME, Source.class));
        source = createNamedInstance(SOURCE_NAME, Source.class);
        act1 = createActivity("Activity1", "Code1", source, ActivityType.OTHER);
        act2 = createActivity("Activity2", "Code2", source, ActivityType.INTERVENTION);
        act3 = createActivity("Activity3", "Code3", source, ActivityType.OTHER);
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
}
