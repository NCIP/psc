package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import org.dom4j.Element;

import static java.text.MessageFormat.format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * @author Saurabh Agrawal
 */
public class AmendmentApprovalXmlSerializerTest extends StudyCalendarXmlTestCase {

    private AmendmentApprovalXmlSerializer serializer;

    private Site site;

    private AmendmentApproval amendmentApproval;
    private Amendment amendment;
    private Study study;
    private StudySite studySite;
    private Date currentDate;
    private StudySiteXmlSerializer studySiteXmlSerializer;
    private AmendmentXmlSerializer amendmentSerializer;

    protected void setUp() throws Exception {
        super.setUp();


        serializer = new AmendmentApprovalXmlSerializer();
        studySiteXmlSerializer = new StudySiteXmlSerializer();
        serializer.setStudySiteXmlSerializer(studySiteXmlSerializer);
        amendmentSerializer = new AmendmentXmlSerializer();
        serializer.setAmendmentSerializer(amendmentSerializer);

        amendment = new Amendment();
        amendment.setMandatory(true);
        amendment.setName("Amendment 1");
        amendment.setDate(createDate(2008, Calendar.JANUARY, 2));
        study = createNamedInstance("Cancer Study", Study.class);
        site = createNamedInstance("Northwestern University", Site.class);
        study.setAmendment(amendment);
        studySite = Fixtures.createStudySite(study, site);
        currentDate = new Date();
        studySite.approveAmendment(amendment, currentDate);
        amendmentApproval = studySite.getAmendmentApprovals().get(0);

    }

    public void testCreateElement() {
        Element actual = serializer.createElement(amendmentApproval);
        assertEquals("Wrong element name", XsdElement.AMENDMENT_APPROVALS.xmlName(), actual.getName());
        assertEquals("Wrong number of children", 1, actual.elements().size());
        Element actualElement = (Element) actual.elements().get(0);

        assertEquals("Wrong element name", XsdElement.AMENDMENT_APPROVAL.xmlName(), actualElement.getName());

        final List contents = actualElement.content();
        assertEquals("Wrong number of content", 2, contents.size());

        assertEquals("Wrong number of attribute", 1, actualElement.attributes().size());

        assertNotNull("Wrong date", actualElement.attributeValue("date"));


    }

    public void testCreateElementForInvalidValues() {
        try {
            serializer.createElement(amendmentApproval);
        } catch (StudyCalendarValidationException e) {
            fail("amendmentApproval can not be null");

        }
    }

    public void testReadElementNotSupported() {

        try {
            serializer.readElement(serializer.createElement(amendmentApproval));
        } catch (UnsupportedOperationException e) {

        }

    }

    public void testCreateDocumentString() throws Exception {


        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("<amendment-approvals ");
        expected.append(format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
        expected.append(format("       {0}:{1}=\"{2} {3}\"", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));
        expected.append(format("       {0}:{1}=\"{2}\">", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));

        expected.append(format("<amendment-approval  date=\"{0}\" >", formatter.format(amendmentApproval.getDate())));
        expected.append(format("<study-site-link study-name=\"{0}\" site-name=\"{1}\"/>",
                amendmentApproval.getStudySite().getStudy().getName(),
                amendmentApproval.getStudySite().getSite().getAssignedIdentifier()));

        expected.append(format("<amendment name=\"{0}\" date=\"{1}\"  mandatory=\"{2}\"/>",
                amendmentApproval.getAmendment().getName(),
                formatter.format(amendmentApproval.getAmendment().getDate()), amendmentApproval.getAmendment().isMandatory()));

        expected.append(format("</amendment-approval>"));

        expected.append("</amendment-approvals>");


        String actual = serializer.createDocumentString(amendmentApproval);
        verifyMocks();
        log.info("actual:" + actual);
        log.info("expected:" + expected.toString());
        assertXMLEqual(expected.toString(), actual);
    }

}

