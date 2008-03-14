package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
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
import static org.easymock.EasyMock.expect;

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

    private SiteDao siteDao;

    private StudyDao studyDao;

    private AmendmentDao amendmentDao;

    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerMockFor(StudyDao.class);
        siteDao = registerMockFor(SiteDao.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);

        serializer = new AmendmentApprovalXmlSerializer();
        studySiteXmlSerializer = new StudySiteXmlSerializer();
        studySiteXmlSerializer.setSiteDao(siteDao);
        studySiteXmlSerializer.setStudyDao(studyDao);

        amendmentSerializer = new AmendmentXmlSerializer();
        serializer.setAmendmentDao(amendmentDao);
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
        assertEquals("Wrong number of content", 0, contents.size());

        assertEquals("Wrong number of attribute", 2, actualElement.attributes().size());

        assertNotNull("Wrong date", actualElement.attributeValue("date"));
        assertNotNull("Wrong date", actualElement.attributeValue("amendment"));


    }

    public void testReadElement() {
        expect(amendmentDao.getByNaturalKey("2008-01-02~Amendment 1")).andReturn(amendmentApproval.getAmendment());

        replayMocks();

        final AmendmentApproval expectedAmendmentApproval = serializer.readElement(serializer.createElement(amendmentApproval, true));
        verifyMocks();

        assertEquals(formatter.format(amendmentApproval.getDate()), formatter.format(expectedAmendmentApproval.getDate()));
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
        expected.append(format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
        expected.append(format("       {0}:{1}=\"{2} {3}\"", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));
        expected.append(format("       {0}:{1}=\"{2}\">", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));

        expected.append(format("<amendment-approval date=\"{0}\" amendment=\"{1}\"/>", formatter.format(amendmentApproval.getDate()), amendmentApproval.getAmendment().getNaturalKey()));

        expected.append("</amendment-approvals>");

        String actual = serializer.createDocumentString(amendmentApproval);

        log.info("actual:" + actual);
        log.info("expected:" + expected.toString());
        assertXMLEqual(expected.toString(), actual);
    }

}

