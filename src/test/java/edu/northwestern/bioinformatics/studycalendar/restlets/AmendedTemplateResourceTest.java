package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.PlannedCalendarXmlSerializer;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import static org.easymock.EasyMock.*;
import org.restlet.data.Status;

import java.util.Calendar;

/**
 * @author John Dzak
 */
public class AmendedTemplateResourceTest extends AuthorizedResourceTestCase<AmendedTemplateResource> {
    public static final String SOURCE_NAME = "Mutant Study";
    public static final String SOURCE_NAME_ENCODED = "Mutant%20Study";

    private static final String AMENDMENT_KEY = "2007-10-19~Amendment B";
    private static final String AMENDMENT_KEY_ENCODED = "2007-10-19~Amendment%20B";

    private Study study;
    private Amendment amendment;
    private PlannedCalendar calendar;
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private AmendmentService amendmentService;
    private PlannedCalendarXmlSerializer calSerializer;

    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        amendmentService = registerMockFor(AmendmentService.class);
        calSerializer = registerMockFor(PlannedCalendarXmlSerializer.class);

        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), SOURCE_NAME_ENCODED);
        request.getAttributes().put(UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), AMENDMENT_KEY_ENCODED);

        calendar = new PlannedCalendar();

        study = createNamedInstance(SOURCE_NAME, Study.class);
        study.setPlannedCalendar(calendar);

        amendment = new Amendment();
        amendment.setName("Amendment B");
        amendment.setDate(createDate(2007, Calendar.OCTOBER, 19));

        calSerializer.setSerializeEpoch(true);
    }

    protected AmendedTemplateResource createResource() {
        AmendedTemplateResource resource = new AmendedTemplateResource();
        resource.setStudyDao(studyDao);
        resource.setAmendmentDao(amendmentDao);
        resource.setXmlSerializer(calSerializer);
        resource.setAmendmentService(amendmentService);
        return resource;
    }

    public void testGet() throws Exception {
        expectFoundStudy(study);
        expectFoundAmendment(amendment);
        expectAmendClonedStudy(amendment);
        expectObjectXmlized(calendar);

        doGet();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    ////// Expect Methods

    protected void expectObjectXmlized(PlannedCalendar cal) {
        expect(calSerializer.createDocumentString(cal)).andReturn(MOCK_XML);
    }

    private void expectFoundStudy(Study study) {
        expect(studyDao.getByAssignedIdentifier(SOURCE_NAME)).andReturn(study);
    }

    private void expectFoundAmendment(Amendment amendment) {
        expect(amendmentDao.getByNaturalKey(AMENDMENT_KEY)).andReturn(amendment);
    }

    private void expectAmendClonedStudy(Amendment target) {
        expect(amendmentService.getAmendedStudy((Study) notNull(), eq(target))).andReturn(study);
    }
}
