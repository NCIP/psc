package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.UserActionService;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Variant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSubject;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.DATA_READER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_TEAM_ADMINISTRATOR;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class UndoableActionsResourceTest  extends AuthorizedResourceTestCase<UndoableActionsResource> {
    private static final String SUBJECT_IDENTIFIER = "1111";

    private SubjectDao subjectDao;
    private Subject subject;
    private UserActionService userActionService;

    public void setUp() throws Exception {
        super.setUp();
        subjectDao = registerDaoMockFor(SubjectDao.class);
        userActionService = registerMockFor(UserActionService.class);
        subject = setGridId("grid1", createSubject("1111", "Perry", "Duglas", createDate(1980, Calendar.JANUARY, 15, 0, 0, 0), Gender.MALE));
        subject.setId(11);
        request.getAttributes().put(UriTemplateParameters.SUBJECT_IDENTIFIER.attributeName(), SUBJECT_IDENTIFIER);
    }

    @Override
    protected UndoableActionsResource createAuthorizedResource() {
        UndoableActionsResource resource = new UndoableActionsResource();
        resource.setSubjectDao(subjectDao);
        resource.setUserActionService(userActionService);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testGetWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.GET,
            STUDY_SUBJECT_CALENDAR_MANAGER,
            STUDY_TEAM_ADMINISTRATOR,
            DATA_READER);
    }

    public void test403WhenUserCannotAccessScheduleUndoableActions() throws Exception {
        setCurrentUser(AuthorizationObjectFactory.createPscUser("bad", PscRole.SYSTEM_ADMINISTRATOR));
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_FORBIDDEN);
    }

    public void testGet400WhenNoSubjectIdentifierInRequest() throws Exception {
        UriTemplateParameters.SUBJECT_IDENTIFIER.removeFrom(request);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void testGet404WhenUnknownSubject() throws Exception {
        expect(subjectDao.findSubjectByPersonId(SUBJECT_IDENTIFIER)).andReturn(null);
        expect(subjectDao.getByGridId(SUBJECT_IDENTIFIER)).andReturn(null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testAvailableAsJsonOnly() throws Exception {
        doInitOnly();
        List<Variant> actual = getResource().getVariants();
        assertEquals("Wrong number of variants: " + actual, 1, actual.size());
        assertEquals("Wrong variant", MediaType.APPLICATION_JSON, actual.get(0).getMediaType());
    }

    public void testGet404WhenNoUndoableActions() throws Exception {
        request.getAttributes().put(UriTemplateParameters.SUBJECT_IDENTIFIER.attributeName()+ ".json",SUBJECT_IDENTIFIER);
        expect(subjectDao.findSubjectByPersonId(SUBJECT_IDENTIFIER)).andReturn(subject);
        expect(userActionService.getUndoableActions("http://trials.etc.edu/studycalendar/api/v1/subjects/grid1/schedules"))
                .andReturn(new ArrayList<UserAction>());
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGetJSONRepresentation() throws Exception {
        request.getAttributes().put(UriTemplateParameters.SUBJECT_IDENTIFIER.attributeName()+ ".json",SUBJECT_IDENTIFIER);
        expect(subjectDao.findSubjectByPersonId(SUBJECT_IDENTIFIER)).andReturn(subject);
        expect(userActionService.getUndoableActions("http://trials.etc.edu/studycalendar/api/v1/subjects/grid1/schedules"))
                .andReturn(Arrays.asList(new UserAction("Delayed for 4 days", "context", "delay",
                        false, AuthorizationObjectFactory.createCsmUser(11, "perry"))));
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("Result is not of right content type", MediaType.APPLICATION_JSON, response.getEntity().getMediaType());
    }



}
