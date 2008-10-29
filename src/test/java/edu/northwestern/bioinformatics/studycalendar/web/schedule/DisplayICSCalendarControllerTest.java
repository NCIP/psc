package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;

/**
 * @author Saurabh Agrawal
 */
@SuppressWarnings("unchecked")
public class DisplayICSCalendarControllerTest extends junit.framework.TestCase {

	protected MockHttpServletRequest request;

	protected MockHttpServletResponse response;

	private DisplayICSCalendarController controller;

	private String grid = "adefg-higj";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		controller = new DisplayICSCalendarController();
		controller.setStudySubjectAssignmentDao (new MockStudySubjectDao());

	}

	public void testHandle() throws Exception {
		request.setMethod("GET");

		request.setPathInfo("/cal/schedule/display/" + grid + ".ics");
		controller.handleRequest(request, response);

		assertEquals("response should return content type of calendar", "text/calendar", response.getContentType());
		assertEquals("response should return file of type ics", "attachment; filename=lastName-firstName-.ics",
				response.getHeader("Content-Disposition"));

	}

	public void testSupportedMethod() throws Exception {
		request.setMethod("POST");
		try {
			controller.handleRequest(request, response);
			fail("post not supported");
		}
		catch (org.springframework.web.HttpRequestMethodNotSupportedException e) {

		}
		String grid = "adefg-higj";
		request.setPathInfo("/cal/schedule/display/" + grid + ".ics");
		request.setMethod("GET");
		controller.handleRequest(request, response);

	}

	private class MockStudySubjectDao extends StudySubjectAssignmentDao {
		@Override
		public StudySubjectAssignment getByGridId(final String gridId) {
			StudySubjectAssignment studySubjectAssignment = new StudySubjectAssignment();
			studySubjectAssignment.setGridId(gridId);
			Subject subject = Fixtures.createSubject("firstName", "lastName");
			studySubjectAssignment.setSubject(subject);

			return studySubjectAssignment;
		}
	}
}