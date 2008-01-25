package edu.northwestern.bioinformatics.studycalendar.testing;

import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XMLValidator.TEMPLATE_VALIDATOR_INSTANCE;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.nwu.bioinformatics.commons.StringUtils;
import static org.apache.commons.lang.StringUtils.EMPTY;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import static org.springframework.validation.ValidationUtils.invokeValidator;
import org.xml.sax.SAXException;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
        log.debug("Expected:\n{}", expectedNormalized);
        log.debug("Actual:\n{}", actualNormalized);
        XMLAssert.assertXMLEqual(expectedNormalized, actualNormalized);
    }

    public void validate(byte[] byteOutput) {
        BindException errors = new BindException(byteOutput, EMPTY);
        invokeValidator(TEMPLATE_VALIDATOR_INSTANCE, new ByteArrayInputStream(byteOutput), errors);

        assertFalse("Template xml should be error free", errors.hasErrors());
    }

    public static PlanTreeNode eqGridId(PlanTreeNode in) {
        EasyMock.reportMatcher(new GridIdMatcher(in));
        return null;
    }

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
            buffer.append(")");

        }
    }
}
