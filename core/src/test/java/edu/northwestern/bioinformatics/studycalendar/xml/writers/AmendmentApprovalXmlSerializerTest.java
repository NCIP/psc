/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import org.dom4j.Element;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * @author Saurabh Agrawal
 */
public class AmendmentApprovalXmlSerializerTest extends StudyCalendarXmlTestCase {
    private AmendmentApprovalXmlSerializer serializer;

    private AmendmentApproval amendmentApproval;
    private Study study;
    private Amendment amendment;

    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serializer = new AmendmentApprovalXmlSerializer();
        amendment = new Amendment();
        amendment.setMandatory(true);
        amendment.setName("Amendment 1");
        amendment.setDate(createDate(2008, Calendar.JANUARY, 2));
        study = createNamedInstance("Cancer Study", Study.class);
        Site site = createNamedInstance("Northwestern University", Site.class);
        study.setAmendment(amendment);
        StudySite studySite = Fixtures.createStudySite(study, site);
        Date currentDate = new Date();
        studySite.approveAmendment(amendment, currentDate);
        amendmentApproval = studySite.getAmendmentApprovals().get(0);
        serializer.setStudy(study);
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(amendmentApproval);
        assertEquals("Wrong element name", XsdElement.AMENDMENT_APPROVAL.xmlName(), actual.getName());

        assertEquals("Wrong element name", XsdElement.AMENDMENT_APPROVAL.xmlName(), actual.getName());

        final List contents = actual.content();
        assertEquals("Wrong number of content", 0, contents.size());

        assertEquals("Wrong number of attribute", 2, actual.attributes().size());

        assertNotNull("Wrong date", actual.attributeValue("date"));
        assertNotNull("Wrong date", actual.attributeValue("amendment"));
    }

    public void testReadElement() {
        replayMocks();
        final AmendmentApproval expectedAmendmentApproval = serializer.readElement(serializer.createElement(amendmentApproval, true));
        verifyMocks();
        assertEquals(formatter.format(amendmentApproval.getDate()), formatter.format(expectedAmendmentApproval.getDate()));
        assertSame("Amendment date is not same", amendment.getDate(), amendmentApproval.getAmendment().getDate());
        assertEquals("Amendment name is not same", amendment.getName(), amendmentApproval.getAmendment().getName());
    }

    public void testCreateElementForInvalidValues() {
        try {
            serializer.createElement(amendmentApproval);
        } catch (StudyCalendarValidationException e) {
            fail("amendmentApproval can not be null");

        }
    }

    public void testCreateDocumentString() throws Exception {
        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("<amendment-approvals ");
        expected.append(MessageFormat.format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
        expected.append(MessageFormat.format("       {0}:{1}=\"{2} {3}\"", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));
        expected.append(MessageFormat.format("       {0}:{1}=\"{2}\">", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));

        expected.append(MessageFormat.format("<amendment-approval date=\"{0}\" amendment=\"{1}\"/>", formatter.format(amendmentApproval.getDate()), amendmentApproval.getAmendment().getNaturalKey()));

        expected.append("</amendment-approvals>");

        String actual = serializer.createDocumentString(Collections.singleton(amendmentApproval));

        log.info("actual:" + actual);
        log.info("expected:" + expected.toString());
        assertXMLEqual(expected.toString(), actual);
    }

}

