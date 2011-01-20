package edu.northwestern.bioinformatics.studycalendar.xml;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer.*;

/**
 * This is a test for behaviors implemented in {@link AbstractStudyCalendarXmlSerializer}.
 *
 * @author Rhett Sutphin
 */
public class StudyCalendarXmlSerializerTest extends StudyCalendarXmlTestCase {
    private KeyValueXmlSerializer serializer;
    private Map<String,String> sampleMap;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serializer = new KeyValueXmlSerializer();
        sampleMap = new MapBuilder<String, String>().
            put("A", "The Fonze").
            put("B", "Honey").
            put("C", "My House From Here").
            toMap();
    }

    public void testCreatedDocumentIncludesPscNamespace() throws Exception {
        Document actual = serializer.createDocument(sampleMap);
        assertEquals(PSC_NS,actual.getRootElement().getNamespace().getURI());
    }

    public void testCreatedDocumentUsesCreatedElement() throws Exception {
        Document actual = serializer.createDocument(sampleMap);
        assertEquals("map", actual.getRootElement().getName());
        assertEquals("Wrong number of children",
            3, actual.getRootElement().elements("entry").size());
    }

    public void testReadDocumentReadsTheRootElement() throws Exception {
        Map<String, String> actual = serializer.readDocument(IOUtils.toInputStream(
            String.format("<map xmlns=\"%s\"><entry key=\"foo\">quux</entry></map>", PSC_NS)));

        assertEquals("Wrong number of things read from document", 1, actual.size());
        assertEquals("Wrong thing read", "quux", actual.get("foo"));
    }

    public void testReadDocumentThrowsXmlExceptionForPoorlyFormedXml() throws Exception {
        try {
            serializer.readDocument(IOUtils.toInputStream("<map foo/>"));
            fail("Exception not thrown");
        } catch (StudyCalendarXmlParsingException scxe) {
            System.out.println(scxe);
            assertContains(scxe.getMessage(), "foo");
        }
    }

    /**
     * Trivial serializer that creates a map from a document like this:
     *
     * <map xmlns="http://bioinformatics.northwestern.edu/ns/psc">
     *   <entry key="a">b</entry>
     * </map>
     */
    private static class KeyValueXmlSerializer extends AbstractStudyCalendarXmlSerializer<Map<String, String>> {
        @Override
        public Element createElement(Map<String, String> object) {
            Element e = element("map");
            for (Map.Entry<String, String> entry : object.entrySet()) {
                e.add(element("entry").addAttribute("key", entry.getKey()).addText(entry.getValue()));
            }
            return e;
        }

        @Override
        @SuppressWarnings( { "unchecked" })
        public Map<String, String> readElement(Element element) {
            Map<String, String> result = new LinkedHashMap<String, String>();
            Iterator<Element> eltit = element.elementIterator("entry");
            while (eltit.hasNext()) {
                Element entry = eltit.next();
                result.put(entry.attributeValue("key"), entry.getText());
            }
            return result;
        }
    }
}
