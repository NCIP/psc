/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSubject;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;

import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class SubjectsResourceTest  extends AuthorizedResourceTestCase<SubjectsResource>{
    private SubjectService subjectService;
    private PscUserService pscUserService;
    private Subject subject;
    @Override
    public void setUp() throws Exception {
        super.setUp();

        subjectService = registerMockFor(SubjectService.class);
        pscUserService = registerMockFor(PscUserService.class);
        subject = createSubject("1111", "Perry", "Duglas", createDate(1980, Calendar.JANUARY, 15, 0, 0, 0), Gender.MALE);
        subject.setId(11);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected SubjectsResource createAuthorizedResource() {
        SubjectsResource resource = new SubjectsResource();
        resource.setSubjectService(subjectService);
        resource.setPscUserService(pscUserService);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testGetWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.GET, STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    public void testGetJSONRepresentation() throws Exception {
        String searchString = "s";
        QueryParameters.Q.putIn(request, searchString);
        List<Subject> expectedSubjects = Arrays.asList(subject);
        expect(subjectService.getFilteredSubjects(searchString)).andReturn(expectedSubjects);
        expect(pscUserService.getVisibleAssignments(getCurrentUser())).andReturn(new ArrayList<UserStudySubjectAssignmentRelationship>());
        request.getClientInfo().setAcceptedMediaTypes(Arrays.asList(new Preference<MediaType>(MediaType.APPLICATION_JSON)));

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("Result is not of right content type", MediaType.APPLICATION_JSON, response.getEntity().getMediaType());
    }
}

