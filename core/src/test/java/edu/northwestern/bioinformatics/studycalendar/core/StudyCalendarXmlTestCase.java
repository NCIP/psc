/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core;

import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import edu.nwu.bioinformatics.commons.StringUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XMLValidator.SPRING_TEMPLATE_VALIDATOR_INSTANCE;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.springframework.validation.ValidationUtils.invokeValidator;

public abstract class StudyCalendarXmlTestCase extends StudyCalendarTestCase {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final SAXReader saxReader = new SAXReader();

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public void assertXMLEqual(String expected, String actual) throws SAXException, IOException {
        validate(actual.getBytes());
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        // XMLUnit's whitespace stripper stopped working at r1976 (of PSC) or so
        // This is a quick-and-dirty (and not necessarily correct) alternative
        String expectedNormalized = StringUtils.normalizeWhitespace(expected);
        String actualNormalized = StringUtils.normalizeWhitespace(actual);

        // XMLUnit interprets whitespace between angle brackets as a child node.
        // There is no way to turn this off, so we have to remove it.
        expectedNormalized = expectedNormalized.replaceAll("> <", "><");
        actualNormalized = actualNormalized.replaceAll("> <", "><");

        log.debug("Expected:\n{}", expectedNormalized);
        log.debug("Actual:\n{}", actualNormalized);
        XMLAssert.assertXMLEqual(expectedNormalized, actualNormalized);
    }

    public void validate(byte[] byteOutput) {
        BindException errors = new BindException(byteOutput, EMPTY);
        invokeValidator(SPRING_TEMPLATE_VALIDATOR_INSTANCE, new ByteArrayInputStream(byteOutput), errors);

        assertFalse("Template xml should be error free", errors.hasErrors());
    }

    public static String createRootElementString(String rootElementName, String attributes, boolean closed) {
        return String.format("<%s\n  xmlns=\"%s\"\n  %s\n  %s>", rootElementName,
            AbstractStudyCalendarXmlSerializer.PSC_NS,
            attributes == null ? "" : attributes,
            closed ? '/' : "");
    }

    public static <R> R parseDocumentString(StudyCalendarXmlSerializer<R> serializer, String doc) {
        return serializer.readDocument(IOUtils.toInputStream(doc));
    }

    public static <R> Collection<R> parseCollectionDocumentString(
        StudyCalendarXmlCollectionSerializer<R> serializer, String doc
    ) {
        return serializer.readCollectionDocument(IOUtils.toInputStream(doc));
    }

    public static String toDateString(Date date) {
        return (date != null) ? formatter.format(date) : null;
    }

    public static Element elementFromString(String xml) throws DocumentException {
        return saxReader.read(new StringReader(xml)).getRootElement();
    }
}
