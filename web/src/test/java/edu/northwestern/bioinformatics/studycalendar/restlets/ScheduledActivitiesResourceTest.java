/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.DATA_READER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_TEAM_ADMINISTRATOR;
import static org.easymock.EasyMock.expect;

import org.restlet.data.Method;
import org.restlet.data.Status;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Saurabh Agrawal
 * @author Rhett Sutphin
 */
public class ScheduledActivitiesResourceTest extends AuthorizedResourceTestCase<ScheduledActivitiesResource> {
    private static final Date REQUESTED_DATE = DateTools.createDate(2008, Calendar.AUGUST, 12);

    private ScheduledActivityDao scheduledActivityDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;

    private StudySubjectAssignment studySubjectAssignment;
    private Study study;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
        studySubjectAssignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);

        study = Fixtures.createSingleEpochStudy("AG 0701", "QoL");
        studySubjectAssignment = Fixtures.createAssignment(
            study,
            Fixtures.createNamedInstance("AG", Site.class),
            Fixtures.createSubject("Jo", "Jo")
        );
        studySubjectAssignment.setGridId("SSA-GRID");

        UriTemplateParameters.ASSIGNMENT_IDENTIFIER.putIn(request, studySubjectAssignment.getGridId());
        UriTemplateParameters.YEAR.putIn(request, "2008");
        UriTemplateParameters.MONTH.putIn(request, "8");
        UriTemplateParameters.DAY.putIn(request, "12");
        UriTemplateParameters.STUDY_IDENTIFIER.putIn(request, study.getAssignedIdentifier());
    }

    @Override
    protected ScheduledActivitiesResource createAuthorizedResource() {
        ScheduledActivitiesResource resource = new ScheduledActivitiesResource();
        resource.setScheduledActivityDao(scheduledActivityDao);
        resource.setScheduledActivityXmlSerializer(xmlSerializer);
        resource.setStudySubjectAssignmentDao(studySubjectAssignmentDao);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId()))
            .andReturn(studySubjectAssignment);
        replayMocks();
        assertAllowedMethods("GET");
    }
    
    public void testGetWithAuthorizedRoles() {
        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId()))
            .andReturn(studySubjectAssignment);
        replayMocks();
        assertRolesAllowedForMethod(Method.GET,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                STUDY_TEAM_ADMINISTRATOR,
                DATA_READER);
    }

    public void testGetXmlForAllScheduledActivitiesForASelectedDate() throws Exception {
        List<ScheduledActivity> scheduledActivityList
            = Arrays.asList(Fixtures.createScheduledActivity("A", 2008, Calendar.AUGUST, 12));

        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId()))
            .andReturn(studySubjectAssignment);
        expect(scheduledActivityDao.getEventsByDate(studySubjectAssignment.getScheduledCalendar(), REQUESTED_DATE, REQUESTED_DATE))
            .andReturn(scheduledActivityList);

        expect(xmlSerializer.createDocumentString(scheduledActivityList)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    public void testGetXmlForAllScheduledActivitiesWhenYearIsNull() throws Exception {
        request.getAttributes().remove(UriTemplateParameters.YEAR.attributeName());

        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId())).andReturn(studySubjectAssignment);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        assertContains(response.getStatus().getDescription(), "Could not parse date from URI");
    }

    public void testGetXmlForAllScheduledActivitiesWhenStudyDoesNotMatch() throws Exception {
        study.setAssignedIdentifier("AG 1701");
        expect(studySubjectAssignmentDao.getByGridId(studySubjectAssignment.getGridId())).andReturn(studySubjectAssignment);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        assertContains(response.getStatus().getDescription(),
            "The designated schedule (SSA-GRID) is not related to the designated study (AG 0701)");
    }
}