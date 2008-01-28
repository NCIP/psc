package edu.northwestern.bioinformatics.studycalendar.testing;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XMLValidator.TEMPLATE_VALIDATOR_INSTANCE;
import edu.nwu.bioinformatics.commons.StringUtils;
import org.apache.commons.io.IOUtils;
import static org.apache.commons.lang.StringUtils.EMPTY;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import static org.springframework.validation.ValidationUtils.invokeValidator;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;

public abstract class StudyCalendarXmlTestCase extends StudyCalendarTestCase {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    public void assertXMLEqual(String expected, String actual) throws SAXException, IOException {
        validate(actual.getBytes());
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        // XMLUnit's whitespace stripper stopped working at r1976 (of PSC) or so
        // This is a quick-and-dirty (and not necessarily correct) alternative
        String expectedNormalized = StringUtils.normalizeWhitespace(expected);
        String actualNormalized = StringUtils.normalizeWhitespace(actual);

        // XMLUnit interprets witespace between angle brackets as a child node.
        // There is no way to turn this off, so we have to remove it.
        expectedNormalized = expectedNormalized.replaceAll("> <", "><");
        actualNormalized = actualNormalized.replaceAll("> <", "><");

        log.debug("Expected:\n{}", expectedNormalized);
        log.debug("Actual:\n{}", actualNormalized);
        XMLAssert.assertXMLEqual(expectedNormalized, actualNormalized);
    }

    public void validate(byte[] byteOutput) {
        BindException errors = new BindException(byteOutput, EMPTY);
        invokeValidator(TEMPLATE_VALIDATOR_INSTANCE, new ByteArrayInputStream(byteOutput), errors);

        assertFalse("Template xml should be error free", errors.hasErrors());
    }

    public static <R> R parseDocumentString(StudyCalendarXmlSerializer<R> serializer, String doc) {
        return serializer.readDocument(IOUtils.toInputStream(doc));
    }

    public static <R> Collection<R> parseCollectionDocumentString(
        StudyCalendarXmlCollectionSerializer<R> serializer, String doc
    ) {
        return serializer.readCollectionDocument(IOUtils.toInputStream(doc));
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public static PlanTreeNode eqGridId(PlanTreeNode in) {
        EasyMock.reportMatcher(new GridIdMatcher(in));
        return null;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public static class GridIdMatcher implements IArgumentMatcher {
        private PlanTreeNode expected;

        public GridIdMatcher(PlanTreeNode expected) {
            this.expected = expected;
        }

        public boolean matches(Object actual) {
            if (!(actual instanceof PlanTreeNode)) {
                return false;
            }
            String actualGridId = ((PlanTreeNode) actual).getGridId();
            return expected.getGridId().equals(actualGridId);
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("eqGridId(");
            buffer.append(expected.getGridId());
            buffer.append(')');
        }
    }
}
